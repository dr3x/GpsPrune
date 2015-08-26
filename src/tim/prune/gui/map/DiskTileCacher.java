package tim.prune.gui.map;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import tim.prune.config.Config;

/**
 * Class to control the reading and saving of map tiles to a cache on disk
 */
public class DiskTileCacher {
	private Connection[] _read;
	private BlockingQueue<Tile>[] _writeQueue;
	private Thread[] _writeThread;
	private String[] _sources;
	private int _numLayers;
	private static final Tile WRITEDONE = new Tile("WRITEDONE", null, null);
	private static final Logger log = 
			Logger.getLogger(DiskTileCacher.class.getName());

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException ex) {
			log.log(Level.SEVERE, "Not able to find SQLite JDBC drivers...", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public DiskTileCacher(String name, int numLayers) {
		_numLayers = numLayers;
		_read = new Connection[_numLayers];
		_writeQueue = (BlockingQueue<Tile>[])new BlockingQueue[_numLayers];
		_writeThread = new Thread[_numLayers];
		_sources = new String[_numLayers];
		String source = "";
		try {
			for (int i = 0; i < _numLayers; i++) {
				source = Config.getConfigString(Config.KEY_DISK_CACHE) + "/"
						+ name + "_" + i + ".sqlite";
				_sources[i] = source;
				_writeQueue[i] = new LinkedBlockingQueue<Tile>();
				_writeThread[i] = new Thread(new TileWriter(source, _writeQueue[i]));
				_writeThread[i].start();
				_read[i] = DriverManager.getConnection("jdbc:sqlite:"
						+ source);
			}
		} catch (SQLException ex) {
			log.log(Level.SEVERE, 
					"Unable to create read connection to: "+source, ex);
		}
	}

	/**
	 * Get the specified tile from the disk cache
	 * 
	 * @param key
	 *            The TileKey for the requested tile.
	 * @return tile image if available, or null if not there
	 */
	public BufferedImage getTile(TileKey key) {
		if (_read == null)
			return null;
		Statement s = null;
		ResultSet rs = null;

		int  l = key.getLayer();
		long z = key.getZoom();
		long x = key.getX();
		long y = key.getY();
		final long index = (((z << z) + x) << z) + y;
		try {
			s = _read[l].createStatement();
			rs = s.executeQuery("select tile from tiles where key = " + index);
			if (rs.next()) {
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(rs
						.getBytes(1)));
				return image;
			}
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Failure to read bytestream for image, "+_sources[l]+": index="+index, ex);
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "SQL Failure reading a tile, "+_sources[l]+": index="+index, ex);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
					log.log(Level.INFO, "SQL Failure closing ResultSet, "+_sources[l], ex);
				}
			if (s != null)
				try {
					s.close();
				} catch (SQLException ex) {
					log.log(Level.INFO, "SQL Failure closing Statement, "+_sources[l], ex);
				}
		}
		return null;
	}

	/**
	 * Save the specified image tile to disk
	 * 
	 * @param tile
	 *            Tile to save.
	 */
	public void saveTile(Tile tile) {
		_writeQueue[tile.getKey().getLayer()].add(tile);
	}

	public void closeDB() {
		for (int i = 0; i < _numLayers; i++) {
			_writeQueue[i].clear();
			_writeQueue[i].add(WRITEDONE);
			try {
				_read[i].close();
			} catch (SQLException ex) {
				log.log(Level.INFO, "SQL Failure closing database, "+_sources[i], ex);
			}
		}
		for (int i = 0; i < _numLayers; i++) {
			try {
				_writeThread[i].join(3000);
			} catch (InterruptedException ex) {
				log.log(Level.INFO, "Interupted waiting on thread join", ex);
			}
		}
	}

	private final class TileWriter implements Runnable {
		private Connection _connection;
		private String _dbName;
		private BlockingQueue<Tile> _queue;

		private void createDB() {
			Statement stat = null;
			try {
				stat = _connection.createStatement();
				stat.execute("CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
			} catch (SQLException ex) {
				log.log(Level.SEVERE, "SQL Failure- unable to create DB, "+_dbName, ex);
			} finally {
				try {
					if (stat != null)
						stat.close();
				} catch (SQLException ex) {
					log.log(Level.INFO, "SQL Failure to close statement, "+_dbName, ex);
				}
			}
		}

		public TileWriter(String dbName, BlockingQueue<Tile> queue) {
			_dbName = dbName;
			_queue = queue;
			File dbFile = new File(dbName);
			boolean create = false;
			try {
				if (!dbFile.exists()) {
					create = true;
				}
				_connection = DriverManager.getConnection("jdbc:sqlite:"
						+ dbFile.getPath());
				if (create)
					createDB();
				// Do not autocommit for faster inserts.
				_connection.setAutoCommit(false);
			} catch (SQLException ex) {
				log.log(Level.SEVERE, "SQL ERROR, unable to create write connection: "+_dbName, ex);
			}
		}

		private Tile nextTile(BlockingQueue<Tile> inQ) {
			Tile tile;
			try {
				tile = inQ.take();
			} catch (InterruptedException ex) {
				return WRITEDONE;
			}
			return tile == null ? WRITEDONE : tile;
		}

		@Override
		public void run() {
			PreparedStatement insert = null;
			int i = 0;
			while (insert == null && i < 10) {
				try {
					insert = _connection
							.prepareStatement("insert into tiles values (?, ?, ?);");
				} catch (SQLException ex) {
					log.log(Level.SEVERE, "SQL ERROR, unable to prepare insert statement: "+_dbName, ex);
				}
			}
			if (insert == null) {
				log.log(Level.SEVERE, "Failed to create insert statement: "+_dbName);
				return; // Things are bad, give up.
			}

			Tile tile;
			while ((tile = nextTile(_queue)) != WRITEDONE) {
				long z = tile.getKey().getZoom();
				long x = tile.getKey().getX();
				long y = tile.getKey().getY();
				final long index = (((z << z) + x) << z) + y;
				ByteArrayOutputStream bstr = null;
				try {
					bstr = new ByteArrayOutputStream();
					ImageIO.write(tile.getImage(), "PNG", bstr);
					insert.setLong(1, index);
					insert.setString(2, tile.getSource());
					insert.setBytes(3, bstr.toByteArray());
					insert.executeUpdate();
				} catch (IOException ex) {
					log.log(Level.SEVERE, "IO ERROR, unable to write image to byte stream", ex);
				} catch (SQLException ex) {
					log.log(Level.SEVERE, "SQL ERROR, unable to insert new tile: "+_dbName, ex);
				} finally {
					if (bstr != null) {
						try {
							bstr.close();
						} catch (IOException ex) {
							log.log(Level.INFO, "IO ERROR, unable to close byte stream", ex);
						}
					}
				}
				// If queue empty them commit, otherwise do more inserts before 
				// commit.
				if (_queue.peek() == null) {
					try {
						_connection.commit();
					} catch (SQLException ex) {
						log.log(Level.SEVERE, "SQL ERROR, unable to commit: "+_dbName, ex);
					}
				}
			}
			try {
				_connection.close();
			} catch (SQLException ex) {
				log.log(Level.INFO, "SQL ERROR, unable to close write connection, "+_dbName, ex);
			}
		}
	}
}
