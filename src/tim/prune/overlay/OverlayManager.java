package tim.prune.overlay;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import tim.prune.draw.Drawable;
import tim.prune.gui.map.MapCanvas;

public class OverlayManager implements Drawable {

	private final List<Overlay> overlays = new ArrayList<Overlay>();
	
	public OverlayManager() {
	}
	
	public void addOverlay( Overlay overlay ) {
		this.overlays.add(overlay);
	}
	
	public void removeOverlay( Overlay overlay ) {
		this.overlays.remove(overlay);
	}
	
	@Override
	public void draw(MapCanvas canvas, Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		for( Overlay o : overlays ) {
			o.draw(canvas, graphics);
		}
	}
}
