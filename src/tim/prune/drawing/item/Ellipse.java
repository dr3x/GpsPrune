package tim.prune.drawing.item;

import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;


public class Ellipse extends Rectangle {
	@Override
	protected RectangularShape createShape() {
		return new Ellipse2D.Double();
	}
}
