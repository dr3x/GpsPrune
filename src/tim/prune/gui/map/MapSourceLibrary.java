package tim.prune.gui.map;

import java.io.File;
import java.util.ArrayList;

import tim.prune.config.Config;

/**
 * Class to hold a library for all the map sources
 * and provide access to each one
 */
public abstract class MapSourceLibrary
{
	/** list of map sources */
	private static ArrayList<MapSource> _sourceList = null;
	/** Number of fixed sources */
	private static int _numFixedSources = 0;
	/** BLOX creates a "cache" directory.  We need to skip it. */
	private static final String BLOX_CACHE = "cache";

	// Static block to initialise source list
	static
	{
		_sourceList = new ArrayList<MapSource>();
		addFixedSources();
		_numFixedSources = _sourceList.size();
		addConfigSources();
	}

	/** Private constructor to block instantiation */
	private MapSourceLibrary() {}


	/** @return number of fixed sources which shouldn't be deleted */
	public static int getNumFixedSources() {
		return _numFixedSources;
	}

	private static boolean hasSqliteData(File baseDir) {
		if(baseDir == null)
			return false;
		if (baseDir.isFile() && baseDir.getName().toLowerCase().endsWith(".sqlite")) {
			return true;
		}
		
		if(isCachePath(baseDir)) {
			return false;
		}

		File[] files = baseDir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (hasSqliteData(f))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the path passed in is our cache path
	 */
	public static boolean isCachePath(File baseDir) {
		if(BLOX_CACHE.equals(baseDir.getName())) {
			return true;
		}
		File cache = new File(Config.getConfigString(Config.KEY_DISK_CACHE));
		return baseDir.equals(cache);
	}
	
	private static boolean appendSqlitePath( String path, StringBuilder fullPath ) {
		if( path != null ) {
			if( fullPath.indexOf(File.pathSeparator + path) < 0 && fullPath.indexOf(path + File.pathSeparator) < 0 ) {
				File pathFile = new File( path );
				if( hasSqliteData(pathFile) ) {
					if( fullPath.length() > 0 ) fullPath.append(File.pathSeparator);
					fullPath.append(pathFile.getAbsolutePath());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Initialize source list by adding bare minimum
	 */
	private static void addFixedSources()
	{	
		String tilesPath = Config.getConfigString(Config.KEY_TILE_LOCATION);
		File path = new File(tilesPath);
		if( !path.exists() ) {
			path.mkdirs();
		}
		String cachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		File cache = new File(cachePath);
		if( !cache.exists() ) {
			cache.mkdirs();
		}

		boolean sqlitesFound = false;
		StringBuilder fullPath = new StringBuilder();
		sqlitesFound |= appendSqlitePath(path.getAbsolutePath(), fullPath);
		sqlitesFound |= appendSqlitePath(System.getenv("TRANSAPPS_TILES_HOME"), fullPath);
		sqlitesFound |= appendSqlitePath(System.getProperty("transapps.tiles.home"), fullPath);
		
		if (sqlitesFound) {
			_sourceList.add(new SqliteMapSource("Transapps Maps", 21, fullPath.toString()));
//			try {
//				_sourceList.add(new OsmMapSource("Transapps (filesystem)", path.toURI().toURL().toExternalForm(), null, 21, ".jpg.tile"));
//			} catch (MalformedURLException e) {
//				JOptionPane.showMessageDialog(null, "Local tiles path '"+path+"' is not valid!\n" + e.getMessage(), 
//						"Error", JOptionPane.ERROR_MESSAGE);
//			}
		}

		_sourceList.add(new OsmMapSource("Mapnik", "http://tile.openstreetmap.org/"));
		_sourceList.add(new OsmMapSource("Osma", "http://tah.openstreetmap.org/Tiles/tile/"));
//		_sourceList.add(new OsmMapSource("Cyclemap", "http://andy.sandbox.cloudmade.com/tiles/cycle/"));
//		_sourceList.add(new OsmMapSource("Reitkarte", "http://topo.geofabrik.de/hills/",
//				"http://topo.openstreetmap.de/topo/", 18));
		_sourceList.add(new MffMapSource("MapsForFree (Relief)", "http://maps-for-free.com/layer/relief/", ".jpg",
				"http://maps-for-free.com/layer/water/", ".gif", 11));
//		_sourceList.add(new CloudmadeMapSource("Pale Dawn", "998", 18));
	}

	/**
	 * Add custom sources from Config to the library
	 */
	private static void addConfigSources()
	{
		String configString = Config.getConfigString(Config.KEY_MAPSOURCE_LIST);
		if (configString != null && configString.length() > 10)
		{
			// Loop over sources in string, separated by vertical bars
			int splitPos = configString.indexOf('|');
			while (splitPos > 0)
			{
				String sourceString = configString.substring(0, splitPos);
				MapSource source = OsmMapSource.fromConfig(sourceString);
				if (source == null) {source = CloudmadeMapSource.fromConfig(sourceString);}
				if (source != null) {
					_sourceList.add(source);
				}
				configString = configString.substring(splitPos+1);
				splitPos = configString.indexOf('|');
			}
		}
	}

	/**
	 * @return current number of sources
	 */
	public static int getNumSources() {
		return _sourceList.size();
	}

	/**
	 * Add the given MapSource to the list (at the end)
	 * @param inSource MapSource object
	 */
	public static void addSource(MapSource inSource) {
		// Check whether source is already there?  Check whether valid?
		_sourceList.add(inSource);
	}

	/**
	 * @param inIndex source index number
	 * @return corresponding map source object
	 */
	public static MapSource getSource(int inIndex)
	{
		// Check whether within range
		if (inIndex < 0 || inIndex >= _sourceList.size()) {return null;}
		return _sourceList.get(inIndex);
	}

	/**
	 * Delete the specified source
	 * @param inIndex index of source to delete
	 */
	public static void deleteSource(int inIndex)
	{
		if (inIndex >= _numFixedSources) {
			_sourceList.remove(inIndex);
		}
	}

	/**
	 * Check whether the given name already exists in the library (case-insensitive)
	 * @param inName name to check
	 * @return true if already exists, false otherwise
	 */
	public static boolean hasSourceName(String inName)
	{
		if (inName == null) {return false;}
		String checkName = inName.toLowerCase().trim();
		for (int i=0; i<getNumSources(); i++)
		{
			String name = getSource(i).getName().toLowerCase();
			if (name.equals(checkName)) {return true;}
		}
		return false;
	}

	/**
	 * @return String containing all custom-added sources as a |-separated list
	 */
	public static String getConfigString()
	{
		StringBuilder builder = new StringBuilder();
		for (int i=getNumFixedSources(); i<getNumSources(); i++) {
			builder.append(getSource(i).getConfigString()).append('|');
		}
		return builder.toString();
	}

	/**
	 * Close oll the on-disk caches for sources.  Should be done on exit.
	 */
	public static void closeCaches() {
		for (MapSource ms : _sourceList) {
			if (ms.getDiskCache() != null) {
				ms.getDiskCache().closeDB();
				ms.setDiskCache(null);
			}
		}
	}
}
