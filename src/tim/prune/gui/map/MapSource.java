package tim.prune.gui.map;

import tim.prune.gui.progress.ProgressMonitor;



/**
 * Class to represent any map source, whether an OsmMapSource
 * or one of the more complicated ones.
 * Map sources may contain just one or several layers, and may
 * build their URLs in different ways depending on the source
 */
public abstract class MapSource
{
	/**
	 * invoked when this map source is enabled.  Throw an exception
	 * if its misconfigured.
	 * 
	 * @param monitor Monitors enable progress
	 */
	public void enable(ProgressMonitor monitor) throws Exception {};

	/**
	 * invoked when this map source is disabled
	 */
	public void disable() {};

	/**
	 * @return true if this guy is remote
	 */
	public abstract boolean isRemote();
	/**
	 * @return the number of layers used in this source
	 */
	public abstract int getNumLayers();

	/**
	 * @return the name of the source
	 */
	public abstract String getName();

	/**
	 * Get an image producer for the given tile info
	 * @param inLayerNum number of layer, from 0 (base) to getNumLayers-1 (top)
	 * @param inZoom zoom level
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @return an image producer
	 */
	public Tile loadTile(TileKey key) throws Exception {
		return getTileLoader().loadTile(key);
	}

	/**
	 * @return the maximum zoom level for this source
	 */
	public abstract int getMaxZoomLevel();

	/**
	 * @return string which can be written to the Config
	 */
	public abstract String getConfigString();

	/**
	 * @return The {@link TileLoader} for this map source
	 */
	protected abstract TileLoader getTileLoader();


	private DiskTileCacher _diskCache = null;

	public void setDiskCache(DiskTileCacher inDiskCache) {
		_diskCache = inDiskCache;
	}

	public DiskTileCacher getDiskCache() {
		return _diskCache;
	}
}
