package tim.prune.gui.map;

import tim.prune.I18nManager;

/**
 * Class to provide a map source for maps-for-free sources
 * These are double-layer sources with jpg and gif tiles
 */
public class MffMapSource extends MapSource
{
	private final class MffTileLoader extends UrlTileLoader {
		private MffTileLoader(MapSource source) {
			super(source);
		}

		@Override
		protected String makeURL(TileKey key) {
			return _baseUrls[key.getLayer()] + "z" + key.getZoom() + 
				"/row" + key.getY() + "/" + key.getZoom() + "_" + key.getX() + 
				"-" + key.getY() + getFileExtension(key.getLayer());
		}

		@Override
		/** Get base url for specified layer */
		public String getBaseUrl(int inLayerNum) {
			return _baseUrls[inLayerNum];
		}

		@Override
		/** site name without protocol or www. */
		public String getSiteName(int inLayerNum) {
			return _siteNames[inLayerNum];
		}

		@Override
		/** Get right file extension for this layer */
		public final String getFileExtension(int inLayerNum) {
			return _extensions[inLayerNum];
		}
	}
	
	
	/** Name for this source */
	private String _name = null;
	/** Base urls */
	private String[] _baseUrls = null;
	/** Site names */
	private String[] _siteNames = null;
	/** File extensions */
	private String[] _extensions = null;
	/** Maximum zoom level */
	private int _maxZoom = 0;
	/** tile loader */
	private MffTileLoader tileLoader;

	/**
	 * Constructor giving name, url and maximum zoom
	 * @param inName source name
	 * @param inUrl base url
	 * @param inMaxZoom maximum zoom level
	 */
	public MffMapSource(String inName, String inUrl1, String inExt1,
		String inUrl2, String inExt2, int inMaxZoom)
	{
		_name = inName;
		if (_name == null || _name.trim().equals("")) {_name = I18nManager.getText("mapsource.unknown");}
		_baseUrls = new String[2];
		_baseUrls[0] = UrlTileLoader.fixBaseUrl(inUrl1);
		_baseUrls[1] = UrlTileLoader.fixBaseUrl(inUrl2);
		_siteNames = new String[2];
		_siteNames[0] = UrlTileLoader.fixSiteName(_baseUrls[0]);
		_siteNames[1] = UrlTileLoader.fixSiteName(_baseUrls[1]);
		_extensions = new String[2];
		_extensions[0] = inExt1;
		_extensions[1] = inExt2;
		_maxZoom = inMaxZoom;
		tileLoader = new MffTileLoader(this);
	}

	/**
	 * @return name
	 */
	public String getName() {
		return _name;
	}

	/** Number of layers is always 2 for mff sources */
	public int getNumLayers() {
		return 2;
	}
	
	@Override
	public boolean isRemote() {
		return !_baseUrls[0].startsWith("file");
	}

	/**
	 * @return maximum zoom level
	 */
	public final int getMaxZoomLevel()
	{
		return _maxZoom;
	}
	
	@Override
	protected TileLoader getTileLoader() {
		return tileLoader;
	}

	/**
	 * @return semicolon-separated list of all fields
	 */
	public String getConfigString()
	{
		// TODO: Maybe a gui will be necessary for this one day
		return "not required";
	}
}
