package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;

public class Mover extends Dragger {
	
	private static final int KEY_DISTANCE = 10;
	
	public Mover( Tool tool ) {
		super( tool, DragHandle.TYPE_DRAWING | DragHandle.TYPE_MOVE );
	}
	
	protected Point2D.Double getMoveDelta(MouseEvent p1, MouseEvent p2) {
		return getMoveDelta(new Point2D.Double(p1.getX(), p1.getY()), new Point2D.Double(p2.getX(), p2.getY()));
	}
	
	protected Point2D.Double getMoveDelta(Point2D.Double p1, Point2D.Double p2) {
		Point2D.Double mp1 = getMapPoint(getInverslyRotatedPoint(p1));
		Point2D.Double mp2 = getMapPoint(getInverslyRotatedPoint(p2));
		
		double deltax = mp2.x - mp1.x;
		double deltay = mp2.y - mp1.y;
		
		return new Point2D.Double(deltax, deltay);
	}
	
	@Override
	public void register(DelegatingMapEventHandler handler) {
		super.register(handler);
		handler.register(KeyEvent.KEY_PRESSED, this);
	}
	
	@Override
	public void handleEvent(AWTEvent e) {
		if( e.getID() == KeyEvent.KEY_PRESSED ) {
			handleKeyPress((KeyEvent) e);
		} else {
			super.handleEvent(e);
		}
	}
	
	@Override
	protected void handleDrag(MouseEvent e) {
		Double moveDelta = getMoveDelta(getLastEvent(), e);
		getItem().move(moveDelta.x, moveDelta.y);
	}
	
	private void handleKeyPress( KeyEvent e ) {
		int code = e.getKeyCode();
		int x = 0;
		int y = 0;			

		switch (code) {
		case KeyEvent.VK_UP: y = -KEY_DISTANCE; break;
		case KeyEvent.VK_DOWN: y = KEY_DISTANCE; break;
		case KeyEvent.VK_LEFT: x = -KEY_DISTANCE; break;
		case KeyEvent.VK_RIGHT: x = KEY_DISTANCE; break;
		default: return;
		}

		DrawingItem item = getTool().getItem();
		if(( x != 0 || y != 0 ) && item != null ) {
			Double moveDelta = getMoveDelta(new Point2D.Double(), new Point2D.Double(x, y));
			item.move(moveDelta.x, moveDelta.y);
			getTool().getHandles().update();
			e.consume();
		}
	}
}