package tim.prune.overlay;

import java.awt.Graphics2D;

import tim.prune.draw.Drawable;
import tim.prune.gui.map.MapCanvas;

public abstract class Overlay implements Drawable {

	private boolean visible = true;
	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public final void draw(MapCanvas canvas, Graphics2D graphics) {
		if( visible ) {
			onDraw(canvas, graphics);
		}
	}
	
	protected abstract void onDraw( MapCanvas canvas, Graphics2D graphics );
}
