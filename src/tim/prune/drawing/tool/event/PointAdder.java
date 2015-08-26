package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;
import java.util.List;

import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;

public class PointAdder extends MapEventHandlerDelegate {
	
	private final int maxPoints;
	
	public PointAdder( Tool tool, int maxPoints ) {
		super( tool );
		this.maxPoints = maxPoints;
	}
	
	@Override
	public void register(DelegatingMapEventHandler handler) {
		handler.register(MouseEvent.MOUSE_PRESSED, this);
	}

	@Override
	public void handleEvent(AWTEvent e) {
		MouseEvent me = (MouseEvent) e;
		if( !me.isMetaDown() ) {
			DrawingItem item = getTool().getItem();
			List<Double> points = item.getPoints();
			if( maxPoints < 0 || points.size() < maxPoints ) {
				Double mapPoint = getMapPoint(me);
				item.addPoint(mapPoint);
				getTool().getHandles().update();
				((MouseEvent) e).consume();
			}
		}
	}
}
