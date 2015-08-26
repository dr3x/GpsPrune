package tim.prune.gui.map;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import tim.prune.gui.progress.ProgressMonitor;


public class SqliteMapSource extends MapSource {
	
	private static final Logger log = Logger.getLogger(SqliteMapSource.class.getName());
	
	private final class SqliteTileLoader extends TileLoader {
		
		private volatile int lastUsedConnectionIndex;
		
		public SqliteTileLoader() {
			super( SqliteMapSource.this );
		}
		
		@Override
		public Tile loadTile(TileKey key) {
			int firstIndexToCheck = lastUsedConnectionIndex;
			
			long z = key.getZoom();
			long x = key.getX();
			long y = key.getY();
			long index = ((z << z) + x << z) + y;
			int connectionArrayIndex = -1;
			
			BufferedImage image = null;
			if( firstIndexToCheck > -1 && firstIndexToCheck < openConnections.length ) {
				image = loadTile(index, openConnections[firstIndexToCheck], openConnectionsNames[firstIndexToCheck]);
				connectionArrayIndex = firstIndexToCheck;
			}
			
			if( image == null ) {
				for( int i = 0; i < openConnections.length; i++ ) {
					image = loadTile(index, openConnections[i], openConnectionsNames[i]);
					if( image != null ) {
						lastUsedConnectionIndex = i;
						connectionArrayIndex = 1;
						break;
					}
				}
			}
			
			return image == null ? null : 
				new Tile(openConnectionsNames[connectionArrayIndex], image, key);
		}

		private BufferedImage loadTile(long index, Connection c, String name) {
			if( c == null ) {
				return null;
			}
			
			Statement s = null;
			ResultSet rs = null;
			
			try {
				s = c.createStatement();
				rs = s.executeQuery("select tile from tiles where key = " + index);
				if( rs.next() ) {					
					BufferedImage image = ImageIO.read(new ByteArrayInputStream(rs.getBytes(1)));
//					TODO: uncomment for tile debugging
//					if( image != null ) {
//						String url = c.getMetaData().getURL();
//						if( url != null ) {
//							int lastIndexOf = url.lastIndexOf('/');
//							if( lastIndexOf > -1 ) {
//								url = url.substring(lastIndexOf + 1);
//							}
//							Graphics graphics = image.getGraphics();
//							graphics.setColor(Color.CYAN);
//							graphics.drawString(url + "@" + index, 5, 15);
//							graphics.setColor(Color.RED);
//							graphics.drawRect(0, 0, image.getWidth(), image.getHeight());
//							graphics.dispose();
//						}
//					}
					return image;
				}
			} catch ( IOException e) {
				log.log(Level.SEVERE, "Failed to read image as byte stream from " + name, e);
			} catch ( SQLException e ) {
				log.log(Level.SEVERE, "SQL Error selecting tile from " + name, e);
			} catch ( Exception e ) {
				log.log(Level.SEVERE, "Failed to read tile from " + name, e);
			} finally {
				try { if( rs != null ) rs.close();} catch ( Exception ignored ) {}
				try { if( s != null ) s.close();} catch ( Exception ignored ) {}
			}
			
			return null;
		}
	}
	
		 
	private final String paths;
	private final String name;
	private final int maxZoomLevel;
	private Connection[] openConnections;
	private String[] openConnectionsNames;
	private SqliteTileLoader tileLoader;
	
	public SqliteMapSource(String name, int maxZoomLevel, String paths) {
		this.paths = paths;
		this.name = name;
		this.maxZoomLevel = maxZoomLevel;
		this.tileLoader = new SqliteTileLoader();
	}
	
	@Override
	public void enable(ProgressMonitor progress) throws Exception {
		progress.start("Loading tile databases", 0);
		List<Connection> connections = new ArrayList<Connection>();
		List<String> connectionNames = new ArrayList<String>();
		String[] split = paths.split(File.pathSeparator);
		for( String s : split ) {
			connectToDbs(new File(s), connections, connectionNames, progress);
		}
		openConnections = connections.toArray(new Connection[0]);
		openConnectionsNames = connectionNames.toArray(new String[0]);
		super.enable(progress);
	}
	
	private void connectToDbs( File baseDir, List<Connection> connections, List<String> connectionNames, ProgressMonitor progress ) {
		if( baseDir.isFile() && baseDir.getName().toLowerCase().endsWith(".sqlite") ) {
			try {
				progress.update(baseDir.getName(), 0);
				Class.forName("org.sqlite.JDBC");
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + baseDir.getAbsolutePath().replace('\\','/'));
				connections.add(connection);
				connectionNames.add(baseDir.getName());
				log.info("Connected to " + baseDir);				
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to connect to " + baseDir, e);
			}
			return;
		}

		if(MapSourceLibrary.isCachePath(baseDir)) {
			return;
		}
		
		File[] files = baseDir.listFiles();
		if( files != null ) {
			progress.update("Scanning " + baseDir.getAbsolutePath(), 0);
			for( File f : files ) {
				connectToDbs(f, connections, connectionNames, progress);
			}
		}
	}
	
	@Override
	public void disable() {
		for( Connection c : openConnections ) {		
			try {
				c.close();
			} catch (SQLException e) {
				log.log(Level.INFO, "SQL Error, failed to close connectio", e);
			}
		}
		openConnections = null;
		super.disable();
	}
	

	@Override
	public int getNumLayers() {
		return 1;
	}
	
	@Override
	public boolean isRemote() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getMaxZoomLevel() {
		return maxZoomLevel;
	}

	@Override
	public String getConfigString() {
		return "sqlite:" + paths;
	}

	@Override
	protected TileLoader getTileLoader() {
		return tileLoader;
	}
}
