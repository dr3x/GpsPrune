package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Stack;

import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.Tool;
import tim.prune.drawing.undo.UndoDrawingEdit;
import tim.prune.gui.map.MapCanvas;
import tim.prune.undo.UndoOperation;

public abstract class Dragger extends MapEventHandlerDelegate {
	
	private final int handleTypes;
	
	private Object dragging;
	private DragHandle handle;
	private MouseEvent lastEvent;
	private MouseEvent firstEvent;
	
	public Dragger( Tool tool, int handleTypes ) {
		super( tool );
		this.handleTypes = handleTypes;
	}
	
	public MouseEvent getLastEvent() {
		return lastEvent;
	}
	
	protected void setLastEvent(MouseEvent lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public MouseEvent getFirstEvent() {
		return firstEvent;
	}
	
	protected void setFirstEvent(MouseEvent firstEvent) {
		this.firstEvent = firstEvent;
	}
	
	public DragHandle getHandle() {
		return handle;
	}
	
	public Object getDragging() {
		return dragging;
	}
	
	protected void setDragging(Object dragging) {
		this.dragging = dragging;
	}
	
	@Override
	public void register(DelegatingMapEventHandler handler) {
		handler.register(MouseEvent.MOUSE_PRESSED, this);
		handler.register(MouseEvent.MOUSE_MOVED, this);
		handler.register(MouseEvent.MOUSE_DRAGGED, this);
		handler.register(MouseEvent.MOUSE_RELEASED, this);
	}
	
	@Override
	public void handleEvent(AWTEvent e) {
		if( ((InputEvent) e).isMetaDown() ) {
			return;
		}
		
		MapCanvas canvas = getTool().getApp().getCanvas();
		MouseEvent me;
		int cursor = Cursor.DEFAULT_CURSOR;
		
		switch (e.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			handlePress((MouseEvent)e);
			cursor = getCursor(dragging);
			break;
		case MouseEvent.MOUSE_MOVED:
		case MouseEvent.MOUSE_DRAGGED:
			me = (MouseEvent) e;
			if( dragging != null ) {				
				handleDrag(me);
				lastEvent = me;
				me.consume();
				getTool().getHandles().update();
				cursor = getCursor(dragging);
			} else {
				Object hit = getHitDragHandle(me);
				if( hit == null ) {
					hit = getHitItem(me);
				}
				cursor = getCursor(hit);
			}
			break;
		case MouseEvent.MOUSE_RELEASED:
			handleRelease((MouseEvent)e);
			break;
		}
		
		canvas.setCursor(Cursor.getPredefinedCursor(cursor));
	}

	protected void handleRelease(MouseEvent e) {
		if( dragging != null ) {
			handleDrag(e);
			e.consume();
			
			// if we've dragged, we have to update the last undo 
			// with the latest state of the object for redo.  This
			// is gross, but it will work for now
			if( lastEvent != firstEvent ) {
				Stack<UndoOperation> undoStack = getTool().getApp().getUndoStack();
				if( !undoStack.isEmpty() ) {
					DrawingItem item = getTool().getItem();
					UndoOperation peek = undoStack.peek();
					if( item != null && peek instanceof UndoDrawingEdit ) {
						((UndoDrawingEdit) peek).setRedoItem(item);
					}
				}
			}
			
			getTool().getHandles().update();
		}
		
		dragging = null;
		handle = null;
		lastEvent = null;
		firstEvent = null;
	}

	protected void handlePress(MouseEvent e) {
		DragHandle hit = getHitDragHandle(e);
		
		if( hit != null ) {
			if( (hit.getType() & handleTypes) > 0 ) {
				dragging = hit;
				handle = hit;
				lastEvent = e;
				firstEvent = e;
				e.consume();
			}
		} else if ( (DragHandle.TYPE_DRAWING & handleTypes) > 0 ) {
			DrawingItem hit2 = getHitItem(e);
			if( hit2 != null && hit2 == getItem() ) {
				dragging = hit2;
				handle = null;
				lastEvent = e;
				firstEvent = e;
				e.consume();
			}
		}
	}
	
	protected DragHandle getHitDragHandle( MouseEvent e ) {
		DragHandles handles = getTool().getHandles();
		MapCanvas canvas = getTool().getApp().getCanvas();
		Graphics2D graphics = (Graphics2D) canvas.getGraphics();
		return handles.hit(canvas, graphics, e.getX(), e.getY());
	}
	
	protected DrawingItem getHitItem( MouseEvent e ) {
		MapCanvas canvas = getTool().getApp().getCanvas();
		Graphics2D graphics = (Graphics2D) canvas.getGraphics();
		Drawing drawing = getTool().getApp().getDrawing();
		DrawingItem item = drawing.hit(canvas, graphics, e.getX(), e.getY());
		if( item != null && getItem() != null && item != getItem() ) {
			return null;
		}
		return item;
	}
	
	protected int getCursor( Object item ) {
		if( item != null ) {
			if( item instanceof DragHandle )
				return Cursor.CROSSHAIR_CURSOR;
			return Cursor.MOVE_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
	}
	
	protected abstract void handleDrag( MouseEvent e );
}