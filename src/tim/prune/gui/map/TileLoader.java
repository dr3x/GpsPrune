package tim.prune.gui.map;



public abstract class TileLoader {
	
	private final MapSource source;
	
	public TileLoader(MapSource source) {
		this.source = source;
	}
	
	protected MapSource getSource() {
		return source;
	}
	
	/**
	 * Get an image producer for the given tile info
	 * @param inLayerNum number of layer, from 0 (base) to getNumLayers-1 (top)
	 * @param inZoom zoom level
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @return an image producer
	 */
	public abstract Tile loadTile(TileKey key) throws Exception;
}
