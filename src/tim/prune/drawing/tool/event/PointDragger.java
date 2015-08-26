package tim.prune.drawing.tool.event;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;

import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;

public class PointDragger extends Dragger {
	public PointDragger( Tool tool ) {
		super( tool, DragHandle.TYPE_POINT | DragHandle.TYPE_LINE_MIDPOINT );
	}

	@Override
	protected void handleDrag(MouseEvent e) {
		DragHandle handle = getHandle();
		if( handle != null ) {
			DrawingItem item = getItem();
			int index = (Integer)handle.getData();
			Double mapPoint2 = getMapPoint(getInverslyRotatedPoint(new Double(e.getX(), e.getY())));
			
			if( handle.getType() == DragHandle.TYPE_POINT ) {
				item.setPoint(mapPoint2, index);
				
			} else if( handle.getType() == DragHandle.TYPE_LINE_MIDPOINT ) {
				item.addPoint(mapPoint2, index);				
				handle.setType(DragHandle.TYPE_POINT);
			}
		}
	}
}