package tim.prune.gui.map;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to act as a memory-based map tile cache
 * For caching of tiles on disk, see the DiskTileCacher class.
 */
public class MemTileCacher
{
	/** Grid size */
	public static final int GRID_SIZE = 15;

	/** Array of List of Tiles to hold tiles */
	private List<Tile>[] _tiles;
	/** Current zoom level */
	private int _zoom = -1;

	@SuppressWarnings("unchecked")
	public MemTileCacher() {
		// _tiles is an array of Lists now so that it can hold two tiles per
		// "slot".  This is necessary now since we have seemless X scrolling
		// and when doing this there are cases were two images will share
		// a common cache index.
		_tiles = (List<Tile>[])new List[GRID_SIZE * GRID_SIZE];
		for (int x = 0; x < _tiles.length; x++) {
			// Should never have more then two images in a "slot".
			_tiles[x] = new ArrayList<Tile>(2);
		}
	}

	/**
	 * Clear the cache if zoom level changed
	 * @param inZoom zoom level
	 */
	public void checkZoom(int inZoom)
	{
		if (inZoom != _zoom)
		{
			_zoom = inZoom;
			clearAll();
		}
	}

	/**
	 * Transform a coordinate from map tiles to array coordinates
	 * @param inTile coordinate of tile
	 * @return coordinate in array (wrapping around cache grid)
	 */
	private static int getCacheCoordinate(int inTile)
	{
		int tile = inTile;
		while (tile >= GRID_SIZE) {tile -= GRID_SIZE;}
		while (tile < 0) {tile += GRID_SIZE;}
		return tile;
	}

	/**
	 * Get the array index for the given coordinates
	 * @param inX x coord of tile
	 * @param inY y coord of tile
	 * @return array index
	 */
	private int getArrayIndex(int inX, int inY)
	{
		int x = getCacheCoordinate(inX);
		int y = getCacheCoordinate(inY);
		return (x + (y * GRID_SIZE));
	}

	/**
	 * Clear all the cached images
	 */
	public void clearAll()
	{
		// Clear all images if zoom changed
		synchronized (_tiles) {
			for (int i = 0; i < _tiles.length; i++) {
				_tiles[i].clear();
			}
		}
	}

	/**
	 * @param inKey The key for the tile to load
	 * @return selected tile if already loaded, or null otherwise
	 */
	public Tile getTile(TileKey inKey)
	{
		if (inKey == null) return null;
		int i = getArrayIndex(inKey.getX(), inKey.getY());
		synchronized (_tiles) {
			for (Tile t : _tiles[i]) {
				if (inKey.equals(t.getKey())) {
					return t;
				}
			}
		}
		return null;
	}

	/**
	 * Save the specified tile at the given coordinates (TileKey in inTile).
	 * @param inTile image to save
	 */
	public void setTile(Tile inTile)
	{
		if (inTile == null || inTile.getKey() == null) {
			return;
		}
		int i = getArrayIndex(inTile.getKey().getX(), inTile.getKey().getY());
		synchronized(_tiles) {
			if (_tiles[i].size() >= 2) _tiles[i].clear();
			_tiles[i].add(inTile);
		}
	}
}
