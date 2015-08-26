package tim.prune.gui.map;

import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

public abstract class UrlTileLoader extends TileLoader {
	
	public UrlTileLoader( MapSource source ) {
		super( source );
	}

	/**
	 * @return the base url for the specified layer
	 */
	public abstract String getBaseUrl(int inLayerNum);

	/**
	 * @return the site name for the specified layer
	 */
	public abstract String getSiteName(int inLayerNum);

	/**
	 * @return the file extension for the specified layer
	 */
	public abstract String getFileExtension(int inLayerNum);
	
	/**
	 * Get an image producer for the given tile info
	 * @param inLayerNum number of layer, from 0 (base) to getNumLayers-1 (top)
	 * @param inZoom zoom level
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @return an image producer
	 */
	public Tile loadTile(TileKey key) throws Exception {
		String makeURL = makeURL(key);
		if( makeURL == null )
			throw new Exception("A valid url or ImageProducer should be provided");
		return new Tile(getSiteName(0), ImageIO.read(new URL(makeURL)), key);
	}
	/**
	 * Make the URL to get the specified tile
	 * @param inLayerNum number of layer, from 0 (base) to getNumLayers-1 (top)
	 * @param inZoom zoom level
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @return URL as string
	 */
	protected abstract String makeURL(TileKey key);

	/**
	 * Make a relative file path from the base directory including site name
	 * @param inLayerNum layer number
	 * @param inZoom zoom level
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @return relative file path as String
	 */
	public String makeFilePath(int inLayerNum, int inZoom, int inX, int inY)
	{
		return getSiteName(inLayerNum) + inZoom + "/" + inX + "/" + inY + getFileExtension(inLayerNum);
	}

	/**
	 * Checks the given url for having the right prefix and trailing slash
	 * @param inUrl url to check
	 * @return validated url with correct prefix and trailing slash, or null
	 */
	public static String fixBaseUrl(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String urlstr = inUrl;
		// check prefix
		try {
			new URL(urlstr);
		}
		catch (MalformedURLException e) {
			// fail if protocol specified
			if (urlstr.indexOf("://") >= 0) {return null;}
			// add the http protocol
			urlstr = "http://" + urlstr;
		}
		// check trailing /
		if (!urlstr.endsWith("/")) {
			urlstr = urlstr + "/";
		}
		// Validate current url, return null if not ok
//		try {
//			URL url = new URL(urlstr);
//			// url host must contain a dot
//			if (url.getHost().indexOf('.') < 0) {return null;}
//		}
//		catch (MalformedURLException e) {
//			urlstr = null;
//		}
		return urlstr;
	}

	/**
	 * Fix the site name by stripping off protocol and www.
	 * This is used to create the file path for disk caching
	 * @param inUrl url to strip
	 * @return stripped url
	 */
	protected static String fixSiteName(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String url = inUrl.toLowerCase();
		int idx = url.indexOf("://");
		if (idx >= 0) {url = url.substring(idx + 3);}
		if (url.startsWith("www.")) {url = url.substring(4);}
		return url;
	}
	
	/**
	 * @return semicolon-separated list of base urls in order
	 */
	public String getSiteStrings()
	{
		StringBuilder sb = new StringBuilder();
		int numLayers = getSource().getNumLayers();
		for (int i=0; i<numLayers; i++) {
			String url = getBaseUrl(i);
			if (url != null) {
				sb.append(url);
				sb.append(';');
			}
		}
		return sb.toString();
	}
}
