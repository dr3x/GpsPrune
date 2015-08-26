package tim.prune.drawing.tool.event;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import tim.prune.draw.Hitable;
import tim.prune.draw.Rotatable;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Polyline;
import tim.prune.gui.map.MapCanvas;
import tim.prune.overlay.Overlay;

public class DragHandles extends Overlay implements Hitable {
	
	public static final DragHandles NULL = new DragHandles(null, 0);

	private final List<DragHandle> handles = new ArrayList<DragHandle>();
	private final DrawingItem item;
	private final int supportedTypes;
	
	public DragHandles( DrawingItem item, int supportedTypes ) {
		this.item = item;
		this.supportedTypes = supportedTypes;
	}
	
	public void update() {
		handles.clear();		
		
		if( item != null ) {
			// add move and rotate handles
			Rectangle2D.Double bounds = item.getBounds();
			if( bounds != null ) {
				Double center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
				updateMoveHandle(center);
				updateRotateHandle(center);
			}

			updatePointsHandles();
			
			if( item instanceof Rotatable ) {
				Rotatable rotatable = (Rotatable) item;
				double rotationAngle = rotatable.getRotationAngle();
				Double rotationPoint = rotatable.getRotationPoint();				
				if( rotationAngle != 0 && rotationPoint != null ) {
					for( DragHandle dh : handles ) {
						dh.setRotation(rotationAngle, rotationPoint);
					}
				}
			}
		}
	}

	private void updateMoveHandle(Double center) {
		if( (supportedTypes & DragHandle.TYPE_MOVE) > 0 ) {
			handles.add(new DragHandle(DragHandle.TYPE_MOVE, center, -1));
		}
	}

	private void updateRotateHandle(Double center) {
		if( (supportedTypes & DragHandle.TYPE_ROTATE) > 0 && item instanceof Rotatable ) {
			DragHandle handle = new DragHandle(DragHandle.TYPE_ROTATE, center, new Point2D.Double(0, -50), center);
			handles.add(handle);
		}
	}

	private void updatePointsHandles() {
		// add handles for each point
		if( (supportedTypes & DragHandle.TYPE_POINT) > 0 ) {				
			List<Point2D.Double> points = item.getPoints();
			if( points.size() > 1 ) {
				for( int i = 0; i < points.size(); i++ ) {
					Double point = points.get(i);
					handles.add(new DragHandle(DragHandle.TYPE_POINT, point, i));
					
					if( i > 0  && (supportedTypes & DragHandle.TYPE_LINE_MIDPOINT) > 0 ) {
						Double lastPoint = points.get(i-1);
						double xdiff = (point.x - lastPoint.x) * .5;
						double ydiff = (point.y - lastPoint.y) * .5;
						Double newPoint = new Double(lastPoint.x + xdiff, lastPoint.y + ydiff);
						handles.add(new DragHandle(DragHandle.TYPE_LINE_MIDPOINT, newPoint, i));
					}
				}
				
				// TODO: this is gross and I hate it
				if( (supportedTypes & DragHandle.TYPE_LINE_MIDPOINT) > 0 && points.size() > 2 && 
						(item instanceof Polyline) && ((Polyline) item).isClosed() ) {
					Double plast = points.get(points.size()-1);
					Double pfirst = points.get(0);
					double xdiff = (plast.x - pfirst.x) * .5;
					double ydiff = (plast.y - pfirst.y) * .5;
					Double newPoint = new Double(pfirst.x + xdiff, pfirst.y + ydiff);
					handles.add(new DragHandle(DragHandle.TYPE_LINE_MIDPOINT, newPoint, points.size()));					
				}
			}
		}
	}
	
	@Override
	protected void onDraw(MapCanvas canvas, Graphics2D graphics) {
		for( DragHandle dh : handles )
			dh.draw(canvas, graphics);	
	}
	
	@Override
	public DragHandle hit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		for( DragHandle dh : handles ) {
			dh = dh.hit(canvas, graphics, x, y);
			if( dh != null )
				return dh;
		}
		return null;
	}
}
