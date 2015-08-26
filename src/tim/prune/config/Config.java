package tim.prune.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Abstract class to hold application-wide configuration
 */
public abstract class Config
{
	/** File from which Config was loaded */
	private static File _configFile = null;

	/** Hashtable containing all config values */
	private static Properties _configValues = new Properties();
	/** Colour scheme object is also part of config */
	private static ColourScheme _colourScheme = new ColourScheme();

	/** Key for track directory */
	public static final String KEY_TRACK_DIR = "prune.trackdirectory";
	/** Key for photo directory */
	public static final String KEY_PHOTO_DIR = "prune.photodirectory";
	/** Key for language code */
	public static final String KEY_LANGUAGE_CODE = "prune.languagecode";
	/** Key for language file */
	public static final String KEY_LANGUAGE_FILE = "prune.languagefile";
	/** Key for GPS device */
	public static final String KEY_GPS_DEVICE = "prune.gpsdevice";
	/** Key for GPS format */
	public static final String KEY_GPS_FORMAT = "prune.gpsformat";
	/** Key for metric/imperial */
	public static final String KEY_METRIC_UNITS = "prune.metricunits";
	/** Key for index of map source */
	public static final String KEY_MAPSOURCE_INDEX = "prune.mapsource";
	/** Key for String containing custom map sources */
	public static final String KEY_MAPSOURCE_LIST = "prune.mapsourcelist";
	/** Key for show map flag */
	public static final String KEY_SHOW_MAP = "prune.showmap";
	/** Key for path to disk cache */
	public static final String KEY_DISK_CACHE = "prune.diskcache";
	/** Key for working online flag */
	public static final String KEY_ONLINE_MODE = "prune.onlinemode";
	/** Key for width of thumbnails in kmz */
	public static final String KEY_KMZ_IMAGE_WIDTH = "prune.kmzimagewidth";
	/** Key for height of thumbnails in kmz */
	public static final String KEY_KMZ_IMAGE_HEIGHT = "prune.kmzimageheight";
	/** Key for gpsbabel path */
	public static final String KEY_GPSBABEL_PATH = "prune.gpsbabelpath";
	/** Key for gnuplot path */
	public static final String KEY_GNUPLOT_PATH = "prune.gnuplotpath";
	/** Key for colour scheme */
	public static final String KEY_COLOUR_SCHEME = "prune.colourscheme";
	/** Key for line width used for drawing */
	public static final String KEY_LINE_WIDTH = "prune.linewidth";
	/** Key for kml track colour */
	public static final String KEY_KML_TRACK_COLOUR = "prune.kmltrackcolour";
	/** Key for tile file location */
	public static final String KEY_TILE_LOCATION = "prune.tilelocation";
	/** Key for base file location */
	public static final String KEY_BASE_FILE_LOCATION = "prune.filelocation";
	/** Key for base support location */
	public static final String KEY_BASE_SUPPORT_LOCATION = "prune.supportlocation";
	/** Key for tile source display */
	public static final String KEY_SHOW_TILE_SOURCE = "prune.showTileSource";
	/** Key for MGRS grid lines display */
	public static final String KEY_SHOW_MGRS_GRID = "prune.showMGRSGrid";
	/** Key for last loc lat */
	public static final String KEY_LAST_CENTER_LAT = "prune.lastCenterLat";
	/** Key for last loc lon */
	public static final String KEY_LAST_CENTER_LON = "prune.lastCenterLon";
	/** Key for last loc lon */
	public static final String KEY_LAST_CENTER_ZOOM = "prune.lastCenterZoom";
	/** Key for last loc lat */
	public static final String KEY_START_CENTER_NAME = "prune.startCenterName";
	/** Key for last loc lat */
	public static final String KEY_START_CENTER_LAT = "prune.startCenterLat";
	/** Key for last loc lon */
	public static final String KEY_START_CENTER_LON = "prune.startCenterLon";
	/** Key for last loc lon */
	public static final String KEY_START_CENTER_ZOOM = "prune.startCenterZoom";
	/** Key for last loc lon */
	public static final String KEY_LAST_SCREEN_WIDTH = "prune.lastScreenWidth";
	/** Key for last loc lon */
	public static final String KEY_LAST_SCREEN_HEIGHT = "prune.lastScreenHeight";

	/**
	 * Save the default configuration file
	 */
	public static void saveDefaultFile()
	{
		if (_configFile == null) {
			Properties props = getDefaultProperties();
			_configFile = new File(props.getProperty(KEY_BASE_SUPPORT_LOCATION)+"/.config");
		}
		FileOutputStream outStream = null;
		try
		{
			outStream = new FileOutputStream(_configFile);
			Config.getAllConfig().store(outStream, "Prune config file");
		}
		catch (IOException ioe) {
			// Happens in background, should log failure.
		}
		finally {
			try {outStream.close();} catch (Exception e) {}
		}
	}

	/**
	 * Load the default configuration file
	 */
	public static void loadDefaultFile()
	{
		Properties props = getDefaultProperties();
		_configFile = new File(props.getProperty(KEY_BASE_SUPPORT_LOCATION)+"/.config");
		try
		{
			loadFile(_configFile);
		}
		catch (ConfigException ce) {} // ignore
	}


	/**
	 * Load configuration from file
	 * @param inFile file to load
	 * @throws ConfigException if specified file couldn't be read
	 */
	public static void loadFile(File inFile) throws ConfigException
	{
		// Start with default properties
		Properties props = getDefaultProperties();
		// Try to load the file into a properties object
		boolean loadFailed = false;
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(inFile);
			props.load(fis);
		}
		catch (Exception e) {
			loadFailed = true;
		}
		finally {
			if (fis != null) try {
				fis.close();
			}
			catch (Exception e) {}
		}
		// Save all properties from file
		_configValues.putAll(props);
		_colourScheme.loadFromHex(_configValues.getProperty(KEY_COLOUR_SCHEME));
		if (loadFailed) {
			throw new ConfigException();
		}
		// Store location of successfully loaded config file
		_configFile = inFile;
	}

	/**
	 * @return Properties object containing default values
	 */
	private static Properties getDefaultProperties()
	{
		Properties props = new Properties();
		// Fill in defaults
		props.put(KEY_GPS_DEVICE, "usb:");
		props.put(KEY_GPS_FORMAT, "garmin");
		props.put(KEY_SHOW_MAP, "1"); // show by default
		props.put(KEY_GNUPLOT_PATH, "gnuplot");
		props.put(KEY_GPSBABEL_PATH, "gpsbabel");
		props.put(KEY_KMZ_IMAGE_WIDTH, "240");
		props.put(KEY_KMZ_IMAGE_HEIGHT, "240");

		String userHome = System.getProperty("user.home");
		String supportLoc = userHome + "/.prune";
		new File(supportLoc).mkdirs();

		String myDocs = System.getenv("CSIDL_MYDOCUMENTS");
		if( myDocs != null ) {
			userHome += "/" + myDocs;
		} else if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			userHome += "/My Documents";
		} else {
			userHome += "/Documents";
		}

		String basePath = userHome + "/TransApps";

		String tilesPath = System.getenv("TRANSAPPS_TILES_HOME");
		if( tilesPath == null ) {
			tilesPath = System.getProperty("transapps.tiles.home");
			if( tilesPath == null ) {
				tilesPath = basePath + "/tiles";
			}
		}

		props.put(KEY_DISK_CACHE, supportLoc+"/map_cache");
		props.put(KEY_TILE_LOCATION, tilesPath);
		props.put(KEY_BASE_FILE_LOCATION, basePath);
		props.put(KEY_BASE_SUPPORT_LOCATION, supportLoc);
		props.put(KEY_SHOW_TILE_SOURCE, "1");
		props.put(KEY_SHOW_MGRS_GRID, "0");
		props.put(KEY_LAST_SCREEN_WIDTH, "1024");
		props.put(KEY_LAST_SCREEN_HEIGHT, "768");

		return props;
	}

	/**
	 * @param inString String to parse
	 * @return int value of String, or 0 if unparseable
	 */
	private static int parseInt(String inString, int def)
	{
		int val = def;
		try {
			val = Integer.parseInt(inString);
		}
		catch (Exception e) {} // ignore, value stays zero
		return val;
	}
	
	/**
	 * @param inString String to parse
	 * @return int value of String, or 0 if unparseable
	 */
	private static double parseDouble(String inString, double def)
	{
		double val = def;
		try {
			val = Double.parseDouble(inString);
		}
		catch (Exception e) {} // ignore, value stays zero
		return val;
	}

	/** @return File from which config was loaded (or null) */
	public static File getConfigFile()
	{
		return _configFile;
	}

	/**
	 * @return config Properties object to allow all config values to be saved
	 */
	public static Properties getAllConfig()
	{
		return _configValues;
	}

	/**
	 * @return the current colour scheme
	 */
	public static ColourScheme getColourScheme()
	{
		return _colourScheme;
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as string
	 */
	public static void setConfigString(String inKey, String inValue)
	{
		if (inValue == null || inValue.equals("")) {
			_configValues.remove(inKey);
		}
		else {
			_configValues.put(inKey, inValue);
		}
		saveDefaultFile();
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as boolean
	 */
	public static void setConfigBoolean(String inKey, boolean inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
			_configValues.put(inKey, (inValue?"1":"0"));
			saveDefaultFile();
		}
	}
	
	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as boolean
	 */
	public static void setConfigDouble(String inKey, double inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
			_configValues.put(inKey, String.valueOf(inValue));
			saveDefaultFile();
		}
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as int
	 */
	public static void setConfigInt(String inKey, int inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
			_configValues.put(inKey, "" + inValue);
			saveDefaultFile();
		}
	}

	/**
	 * Get the given configuration setting as a String
	 * @param inKey key
	 * @return configuration setting as a String
	 */
	public static String getConfigString(String inKey)
	{
		return _configValues.getProperty(inKey);
	}

	/**
	 * Get the given configuration setting as a boolean
	 * @param inKey key
	 * @return configuration setting as a boolean (default to true)
	 */
	public static boolean getConfigBoolean(String inKey)
	{
		String val = _configValues.getProperty(inKey);
		return (val == null || val.equals("1"));
	}

	/**
	 * Get the given configuration setting as an int
	 * @param inKey key
	 * @return configuration setting as an int
	 */
	public static int getConfigInt(String inKey)
	{
		return getConfigInt(inKey, 0);
	}
	
	/**
	 * Get the given configuration setting as an int
	 * @param inKey key
	 * @return configuration setting as an int
	 */
	public static int getConfigInt(String inKey, int def)
	{
		return parseInt(_configValues.getProperty(inKey), def);
	}
	
	/**
	 * Get the given configuration setting as an int
	 * @param inKey key
	 * @return configuration setting as an int
	 */
	public static double getConfigDouble(String inKey, double def)
	{
		return parseDouble(_configValues.getProperty(inKey), def);
	}

	/**
	 * Check whether the given key corresponds to a boolean property
	 * @param inKey key to check
	 * @return true if corresponding property is boolean
	 */
	public static boolean isKeyBoolean(String inKey)
	{
		// Only two boolean keys so far
		return inKey != null && (
			inKey.equals(KEY_METRIC_UNITS) || inKey.equals(KEY_SHOW_MAP));
	}

	/**
	 * Update the colour scheme property from the current settings
	 */
	public static void updateColourScheme()
	{
		setConfigString(KEY_COLOUR_SCHEME, _colourScheme.toString());
	}
}
