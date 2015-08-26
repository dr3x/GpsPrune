package tim.prune.draw;

import java.awt.Graphics2D;

import tim.prune.gui.map.MapCanvas;

public interface Drawable {

	void draw(MapCanvas canvas, Graphics2D graphics);
}
