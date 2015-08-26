package tim.prune.draw;

import java.awt.Graphics2D;

import tim.prune.gui.map.MapCanvas;

public interface Hitable {

	Hitable hit(MapCanvas canvas, Graphics2D graphics, int x, int y);
}
