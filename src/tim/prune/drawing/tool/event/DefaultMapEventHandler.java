package tim.prune.drawing.tool.event;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


public class DefaultMapEventHandler implements MapEventHandler {

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

//	private static final int KEY_DISTANCE = 10;
//	private DragHandle dragging;
//	private MouseEvent lastEvent;
//	private MouseEvent firstEvent;
//	
//	public DefaultMapEventHandler(Tool tool) {
//		super(tool);
//	}
//
//	@Override
//	public void mouseClicked(MouseEvent e) {
//		if( !e.isMetaDown() ) {
//			if( e.getClickCount() == 2 ) {
//				getTool().getApp().getToolbox().deactivate();
//			} else {
//				DrawingItem item = getTool().getItem();
//				item.addPoint(getMapPoint(e));
//				getTool().getHandles().update();
//			}
//			e.consume();
//		}
//	}
//
//	@Override
//	public void mousePressed(MouseEvent e) {
//		if( !e.isMetaDown() ) {
//			startDrag(e);
//		}
//	}
//
//	@Override
//	public void mouseMoved(MouseEvent e) {
//		if( !e.isMetaDown() ) {
//			if( dragging != null ) {
//				updateDrag(e);
//			}
//		}
//	}
//	
//	@Override
//	public void mouseDragged(MouseEvent e) {
//		mouseMoved(e);
//	}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		mouseMoved(e);
//		
//		// if we've dragged, we have to update the last undo 
//		// with the latest state of the object for redo.  This
//		// is gross, but it will work for now
//		if( dragging != null && lastEvent != firstEvent ) {
//			Stack<UndoOperation> undoStack = getTool().getApp().getUndoStack();
//			if( !undoStack.isEmpty() ) {
//				DrawingItem item = getTool().getItem();
//				UndoOperation peek = undoStack.peek();
//				if( item != null && peek instanceof UndoDrawingEdit ) {
//					((UndoDrawingEdit) peek).setRedoItem(item);
//				}
//			}
//		}
//		dragging = null;
//		lastEvent = null;
//		firstEvent = null;
//	}
//	
//	
//	private void startDrag(MouseEvent e) {
//		MapCanvas canvas = getTool().getApp().getCanvas();
//		Graphics2D graphics = (Graphics2D)canvas.getGraphics();
//		
//		DragHandle handle = getTool().getHandles().hit(
//				canvas, graphics, e.getX(), e.getY());
//		DrawingItem item = getTool().getItem();
//		
//		if( handle == null ) {
//			if( item.hit(canvas, graphics, e.getX(), e.getY()) != null ) {
//				dragging = new DragHandle(DragHandle.TYPE_MOVE, 
//						new Point2D.Double(e.getX(), e.getY()), null);
//			}
//		} else {
//			dragging = handle;
//		}
//		
//		if( dragging != null ) {
//			lastEvent = e;
//			firstEvent = e;
//			e.consume();
//		}
//	}
//
//	private void updateDrag(MouseEvent e) {
//		DrawingItem item = getTool().getItem();
//
//		if( dragging.getType() == DragHandle.TYPE_ROTATE ) {
//			
//			if( item instanceof Rotatable ) {
//				Point2D.Double p1 = (Point2D.Double) dragging.getData();
//				Point2D.Double p2 = getMapPoint(e);
//				((Rotatable) item).rotate(getBearing(p1, p2));
//			}
//			
//		} else if ( dragging.getType() == DragHandle.TYPE_POINT ) {				
//			item.setPoint(getMapPoint(e), (Integer)dragging.getData());
//
//		} else {
//
//			Double moveDelta = getMoveDelta(lastEvent, e);
//			item.move(moveDelta.x, moveDelta.y);
//		}
//
//		getTool().getHandles().update();
//		e.consume();
//		lastEvent = e;
//	}
//	
//	
//	@Override
//	public void keyPressed(KeyEvent e) {
//		if( !e.isShiftDown() && !e.isMetaDown() && !e.isAltDown() ) {
//			int code = e.getKeyCode();
//
//			if( KeyEvent.VK_ESCAPE == code ) {
//				getTool().getApp().getToolbox().deactivate();
//				
//			} else {
//				int x = 0;
//				int y = 0;			
//
//				switch (code) {
//				case KeyEvent.VK_UP: y = -KEY_DISTANCE; break;
//				case KeyEvent.VK_DOWN: y = KEY_DISTANCE; break;
//				case KeyEvent.VK_LEFT: x = -KEY_DISTANCE; break;
//				case KeyEvent.VK_RIGHT: x = KEY_DISTANCE; break;
//				}
//
//				DrawingItem item = getTool().getItem();
//				if(( x != 0 || y != 0 ) && item != null ) {
//					Double moveDelta = getMoveDelta(new Point(), new Point(x, y));
//					item.move(moveDelta.x, moveDelta.y);
//					getTool().getHandles().update();
//					e.consume();
//				}
//			}
//		}
//	}
//	
//	@Override
//	public void keyReleased(KeyEvent e) {
//		super.keyReleased(e);
//	}
}