package tim.prune.drawing.tool.event;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import tim.prune.draw.Rotatable;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;

public class Rotator extends Dragger {
	
	private Point2D.Double rotationPoint;
	
	public Rotator( Tool tool ) {
		super( tool, DragHandle.TYPE_ROTATE );
	}
	
	protected double getBearing(Point p1, Point p2) {
		return getBearing(getMapPoint(p1), getMapPoint(p2));
	}
	
	protected double getBearing(Point2D.Double p1, Point2D.Double p2) {
		double deltax = p2.x - p1.x;
		double deltay = p2.y - p1.y;
		double bearing = Math.toDegrees(Math.atan2(deltay, deltax));
		
		// adjust for quad
		bearing = 90d - bearing;
		if( deltax < 0 && deltay > 0 ) {
			bearing = 360 + bearing;
		}

		return bearing;
	}
	
	@Override
	protected void handlePress(MouseEvent e) {
		super.handlePress(e);
	}
	
	@Override
	protected void handleRelease(MouseEvent e) {
		super.handleRelease(e);
		rotationPoint = null;
	}
	
	@Override
	protected void handleDrag(MouseEvent e) {
		DrawingItem item = getItem();
		if( item instanceof Rotatable ) {			
			Point2D.Double p1 = (Point2D.Double) getHandle().getData();
			Point2D.Double p2 = getMapPoint(e);
			Rotatable rotatable = (Rotatable) item;
			rotatable.rotate(getBearing(p1, p2), rotationPoint);
			rotationPoint = rotatable.getRotationPoint();
		}	
	}
}