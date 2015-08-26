package tim.prune.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Externalizable;
import java.util.List;

import tim.prune.draw.Drawable;
import tim.prune.draw.Hitable;
import tim.prune.draw.Moveable;
import tim.prune.gui.map.MapCanvas;

public interface DrawingItem extends Drawable, Hitable, Moveable, Externalizable {

	void setId(long id);

	long getId();

	void setDrawing(Drawing drawing);

	Drawing getDrawing();

	@Mutator(accessor = "getForeground", name = "foreground color")
	void setForeground(Color foreground);

	@Mutator(accessor = "getBackground", name = "background color")
	void setBackground(Color background);

	Color getBackground();

	Color getForeground();

	@Mutator(name = "add point")
	void addPoint(Point2D.Double point);
	
	@Mutator(name = "add point")
	void addPoint(Point2D.Double point, int index);

	@Mutator(name = "remove point")
	void removePoint(Point2D.Double point);

	@Mutator(name = "remove point")
	void removePoint(int index);

	@Mutator(name = "set point")
	void setPoint(Point2D.Double point, int idx);

	@Mutator(name = "set points")
	void setPoints(List<Point2D.Double> p);	

	List<Point2D.Double> getPoints();
	
	Rectangle2D.Double getBounds();

	void setHighlight(Color highlight);

	void applyStyle(DrawingStyle style);

	void updateStyle(DrawingStyle style);
	
	@Override
	DrawingItem hit(MapCanvas canvas, Graphics2D graphics, int x, int y);
}