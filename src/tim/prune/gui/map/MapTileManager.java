package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.gui.progress.ProgressMonitor;

/**
 * Class responsible for managing the map tiles,
 * including invoking the correct memory cacher(s) and/or disk cacher(s)
 */
public class MapTileManager
{
	/** Not found tile */
	private Tile _notFound;
	/** Off the map tile. */
	private Tile _dragonTile;
	/** Tile loaders */
	private ThreadPoolExecutor loaderPool;
	/** Parent object to inform when tiles received */
	private MapCanvas _parent = null;
	/** Current map source */
	private MapSource _mapSource = null;
	/** Array of tile caches, one per layer */
	private MemTileCacher[] _tempCaches = null;
	/** Number of layers */
	private int _numLayers = -1;
	/** Current zoom level */
	private int _zoom = 0;
	/** List of Tiles that already have a load thread. */
	private List<TileKey> _tilesLoading = null;

	private static final Logger log = 
			Logger.getLogger(MapTileManager.class.getName());


	/**
	 * Constructor
	 * @param inParent parent canvas to be informed of updates
	 */
	public MapTileManager(MapCanvas inParent)
	{
		_parent = inParent;
		_notFound = buildNotFoundTile();
		_dragonTile = buildDragonTile();
		_tilesLoading = new LinkedList<TileKey>();
		resetConfig();
	}

	/**
	 * Clear mem caches is zoom changed.
	 * @param inZoom zoom level
	 */
	public void checkZoom(int inZoom)
	{
		if (_zoom != inZoom) {
			_zoom = inZoom;
			if (_tempCaches != null) {
				for (int i = 0; i < _tempCaches.length; i++) {
					_tempCaches[i].checkZoom(inZoom);
				}
			}
		}
	}

	/**
	 * @return true if zoom is too high for tiles
	 */
	public boolean isOverzoomed()
	{
		// Ask current map source what maximum zoom is
		int maxZoom = (_mapSource == null?0:_mapSource.getMaxZoomLevel());
		return (_zoom > maxZoom);
	}

	/**
	 * Clear all the memory caches due to changed config / zoom
	 */
	public void clearMemoryCaches()
	{
		int numLayers = _mapSource == null?0:_mapSource.getNumLayers();
		if (_tempCaches == null || _tempCaches.length != numLayers) {
			// Cachers don't match, so need to create the right number of them
			_tempCaches = new MemTileCacher[numLayers];
			for (int i=0; i<numLayers; i++) {
				_tempCaches[i] = new MemTileCacher();
			}
		}
		else {
			// Cachers already there, just need to be cleared
			for (int i=0; i<numLayers; i++) {
				_tempCaches[i].clearAll();
			}
		}
	}

	/**
	 * Reset the map source configuration, apparently it has changed
	 */
	public void resetConfig()
	{		
		if( loaderPool != null ) {
			loaderPool.shutdownNow();
		}

		loaderPool = new ThreadPoolExecutor(0, 1, 
				60L, TimeUnit.SECONDS,
				new PriorityBlockingQueue<Runnable>(), 
				new Factory());
		loaderPool.execute(new InitTask());
	}

	/**
	 * @return the number of layers in the map
	 */
	public int getNumLayers()
	{
		return _numLayers;
	}

	private TileKey toKey(int inLayer, int inX, int inY) {
		// Rollover to tiles from other side of the map if we need to.
		int max = 1 << _zoom;
		int realX = inX;
		while (realX >= max) realX = realX - max;
		while (realX < 0) realX = max + realX;

		return new TileKey(inLayer, _zoom, realX, inY);
	}
	
	/**
	 * @param inLayer layer number, starting from 0
	 * @param inX x index of tile
	 * @param inY y index of tile
	 * @return selected tile if already loaded, or null otherwise
	 */
	public Image getTileImage(int inLayer, int inX, int inY)
	{
		TileKey key = toKey(inLayer, inX, inY);
		// Check first in memory cache for tile
		MemTileCacher tempCache = _tempCaches!=null&&inLayer<_tempCaches.length?
				_tempCaches[inLayer]:null;
		Tile tile = tempCache == null?null:tempCache.getTile(key);
		if (tile != null) {
			return tile.getImage();
		}

		synchronized(_tilesLoading) {
			for (TileKey k : _tilesLoading) {
				if (k.equals(key)) return null;
			}
			_tilesLoading.add(key);
		}
		// Tile wasn't in memory try to load it from the source or cache
		loaderPool.execute(new MapSourceTask(_mapSource, key));

		return null;
	}
	
	/**
	 * @param inLayer layer number, starting from 0
	 * @param inX x index of tile
	 * @param inY y index of tile
	 * @return selected tile if already loaded, or null otherwise
	 */
	public String getTileInfo(int inLayer, int inX, int inY)
	{
		TileKey key = toKey(inLayer, inX, inY);
		// Check first in memory cache for tile
		Tile tile = null;
		if( (_tempCaches != null) && (inLayer < _tempCaches.length) ) {
			MemTileCacher tempCache = _tempCaches[inLayer];
			tile = tempCache == null?null:tempCache.getTile(key);
		}
		return tile == null ? "" : tile.getSource();
	}

	private static Tile buildNotFoundTile() {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
		String message = "Tile not found";
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		Font font = graphics.getFont();
		font = new Font(font.getName(), Font.BOLD, 7);
		graphics.setFont(font);
		
		FontRenderContext frc = graphics.getFontRenderContext();
		Rectangle2D textBounds = graphics.getFont().getStringBounds(message, frc);
		int x = (int)(image.getWidth() - textBounds.getWidth()) / 2;
		int y = (int)(image.getHeight() - textBounds.getHeight()) / 2;
		
		ColourScheme colourScheme = Config.getColourScheme();
		graphics.setColor(colourScheme.getColour(ColourScheme.IDX_TEXT));
		graphics.setBackground(colourScheme.getColour(ColourScheme.IDX_BACKGROUND));		
		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphics.drawString(message, x, y);
		graphics.dispose();
		return new Tile("", image, null);
	}

	private static Tile buildDragonTile() {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = (Graphics2D) image.getGraphics();

		graphics.setBackground(Color.GRAY);
		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphics.dispose();
		return new Tile("", image, null);
	}

	private final class Factory implements ThreadFactory {
		private int counter;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "MapSourceWorker-" + (counter++));
		}
	}
	
	private static abstract class PriorityTask implements Runnable, Comparable<PriorityTask> {
		private static long seqNumCounter = 0;
		protected final long seqNum = ++seqNumCounter;
		
		@Override
		public int compareTo(PriorityTask o) {
			return (int)(o.seqNum - seqNum);
		}
	}
	
	private final class InitTask extends PriorityTask {
		@Override
		public void run() {
			try {
				_parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				int sourceNum = Config.getConfigInt(Config.KEY_MAPSOURCE_INDEX);

				if( _mapSource != null ) {
					_mapSource.disable();
				}
				
				_mapSource = MapSourceLibrary.getSource(sourceNum);
				if (_mapSource == null) {
					_mapSource = MapSourceLibrary.getSource(0);
				}
				String diskCachePath = Config
						.getConfigString(Config.KEY_DISK_CACHE);
				if (diskCachePath != null && _mapSource.isRemote()) {
					_mapSource.setDiskCache(
							new DiskTileCacher(_mapSource.getName(),
											_mapSource.getNumLayers()));
				}

				ProgressMonitor monitor = new ProgressMonitor();
				try {
					_mapSource.enable(monitor);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Exception enabling map source "+_mapSource.getName(), e);
				}
				monitor.finish();

				clearMemoryCaches();
				_numLayers = _mapSource.getNumLayers();
			} finally {
				_parent.setCursor(Cursor.getDefaultCursor());
				loaderPool.setCorePoolSize(MemTileCacher.GRID_SIZE/3);
				loaderPool.setMaximumPoolSize(MemTileCacher.GRID_SIZE);
				_parent.tilesUpdated(true);
			}
		}
	}

	private final class MapSourceTask extends PriorityTask {
		private final TileKey key;
		private final MapSource source;

		private MapSourceTask(MapSource source, TileKey key) {
			this.source = source;
			this.key = key;
		}

		private void clearLoadingList() {
			synchronized(_tilesLoading) {
				Iterator<TileKey> i = _tilesLoading.iterator();
				while (i.hasNext()) {
					TileKey k = i.next();
					if (k.equals(key)) {
						i.remove();
						break;
					}
				}
			}
		}

		@Override
		public void run() {
			try {
				MemTileCacher cache = _tempCaches[key.getLayer()];
				// If zoom has changed then exit.
				if (key.getZoom() != _zoom) {
					return;
				}

				String diskCachePath = Config
						.getConfigString(Config.KEY_DISK_CACHE);
				boolean onlineMode = Config
						.getConfigBoolean(Config.KEY_ONLINE_MODE);

				boolean useCache = (diskCachePath != null) && source.isRemote();
				boolean useSource = (onlineMode || !source.isRemote());
				boolean fromDiskCache = false;

				Tile tile = null;

				long max = 1 << _zoom;
				if (key.getY() < 0 || key.getY() >= max) {
					// Do not attempt to load a tile that should not exist.
					tile = _dragonTile.cloneWithKey(key);
					useCache = false; // Do not cache the dragon.
				} else {
					try {
						if (useCache) {
							BufferedImage tileImage = null;
							if (source.getDiskCache() != null) {
								tileImage = source.getDiskCache().getTile(key);
							}
							if (tileImage != null) {
								tile = new Tile(source.getName(), tileImage,
										key);
								fromDiskCache = true;
							}
						}

						if (tile == null && useSource) {
							tile = source.loadTile(key);
						}
					} catch (Exception e) {
						cache.setTile(_notFound.cloneWithKey(key));
						log.log(Level.SEVERE, "["
								+ Thread.currentThread().getName()
								+ "] Failed to load " + key + ".  "
								+ e.getMessage(), e);
						_parent.tilesUpdated(false);
					}
				}

				if (tile != null) {
					if (useCache && !fromDiskCache && (tile.getImage()!=null)) {
						if (source.getDiskCache() != null) {
							source.getDiskCache().saveTile(tile);
						}
					}
				} else {
					tile = _notFound.cloneWithKey(key);
				}

				if (key.getZoom() == _zoom) {
					cache.setTile(tile);
				}
				_parent.tilesUpdated(true);
			} finally {
				clearLoadingList();
			}
		}
	}
}
