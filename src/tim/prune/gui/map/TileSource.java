package tim.prune.gui.map;

import java.awt.Point;

public class TileSource extends MapLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final MapCanvas canvas;

	private int[] tileIndices = new int[4];
	private int[] pixelOffsets = new int[2];
	
	public TileSource(MapCanvas canvas, int align) {
		super( align );
		this.canvas = canvas;
	}
	
	@Override
	protected String getText() {
		String source = "";
		Point mp = canvas.getMousePosition();
		MapPosition mapPosition = canvas.getMapPosition();
		
		if( isEnabled() && mapPosition != null && mp != null ) {
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			mapPosition.getDisplayOffsets(width, height, pixelOffsets);
			mapPosition.getTileIndices(width, height, tileIndices);
			int tileX = mapPosition.getTileIndex(mp.x + pixelOffsets[0]) + tileIndices[0];
			int tileY = mapPosition.getTileIndex(mp.y + pixelOffsets[1]) + tileIndices[2];
			MapTileManager tileManager = canvas.getTileManager();
			source = tileManager.getTileInfo(0, tileX, tileY);
		}
		
		return source;
	}
}
