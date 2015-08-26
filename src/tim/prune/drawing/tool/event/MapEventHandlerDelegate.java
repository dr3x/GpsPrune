package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import tim.prune.draw.Rotatable;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;
import tim.prune.gui.map.Projection;

public abstract class MapEventHandlerDelegate {

	private final Tool tool;

	public MapEventHandlerDelegate( Tool tool ) {
		this.tool = tool;
	}

	public Tool getTool() {
		return tool;
	}

	public DrawingItem getItem() {
		return getTool().getItem();
	}

	protected Point2D.Double getMapPoint( MouseEvent e ) {
		Projection projection = tool.getApp().getCanvas().getProjection();
		return projection.fromScreen(e.getX(), e.getY(), null);
	}

	protected Point2D.Double getMapPoint( Point e ) {
		Projection projection = tool.getApp().getCanvas().getProjection();
		return projection.fromScreen((int)e.getX(), (int)e.getY(), null);
	}

	protected Point2D.Double getMapPoint( Point2D.Double p ) {
		Projection projection = tool.getApp().getCanvas().getProjection();
		return projection.fromScreen((int)p.getX(), (int)p.getY(), p);
	}

	protected Point2D.Double getInverslyRotatedPoint( MouseEvent e ) {
		return getInverslyRotatedPoint(new Point2D.Double(e.getX(), e.getY()));
	}

	protected Point2D.Double getInverslyRotatedPoint( Point2D.Double p ) {
		DrawingItem item = getItem();		
		if( item instanceof Rotatable ) {
			Rotatable rotatable = (Rotatable) item;
			double rotationAngle = rotatable.getRotationAngle();
			Double rotationPoint = rotatable.getRotationPoint();
			if( rotationPoint != null && rotationAngle != 0 && item.getPoints().size() > 1 ) {
				Double rotScreen = getTool().getApp().getCanvas().getProjection().toScreen(rotationPoint.y, rotationPoint.x, null);
				if( rotationAngle != 0 && rotationPoint != null ) {
					AffineTransform xform = new AffineTransform();
					xform.rotate(Math.toRadians(rotationAngle), rotScreen.x, rotScreen.y);
					try { xform = xform.createInverse(); } catch (Exception ignored) { ignored.printStackTrace(); }
					xform.transform(p, p);
				}
			}
		}
		return p;
	}


	public abstract void register( DelegatingMapEventHandler handler );
	public abstract void handleEvent( AWTEvent e );
}