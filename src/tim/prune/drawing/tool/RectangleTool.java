package tim.prune.drawing.tool;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Ellipse;
import tim.prune.drawing.item.Rectangle;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.Dragger;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.Rotator;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.BackgroundPropertyTool;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.LineStylePropertyTool;
import tim.prune.drawing.tool.property.LineWidthPropertyTool;

public class RectangleTool extends Tool {

	public RectangleTool(DrawApp app) {
		this("rectangle", app);
	}

	public RectangleTool(String name, DrawApp app) {
		super(name, app);
		
		registerMapEventHandlerDelegate(new RectanglePointDragger(this));
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new Rotator(this));	
		registerMapEventHandlerDelegate(new RectangleMapEventHandler(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, 2));
		
		registerHandleType(DragHandle.TYPE_POINT);
		registerHandleType(DragHandle.TYPE_MOVE);
		registerHandleType(DragHandle.TYPE_ROTATE);
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(BackgroundPropertyTool.class);
		registerPropertyToolType(LineStylePropertyTool.class);
		registerPropertyToolType(LineWidthPropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		Rectangle item = getApp().getDrawing().addItem(Rectangle.class);
		return item;
	}

	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Rectangle
		&& !(itemType instanceof Ellipse);
	}
	
	public class RectanglePointDragger extends Dragger {
		
		private Point2D.Double anchor;
		
		public RectanglePointDragger( Tool tool ) {
			super( tool, DragHandle.TYPE_POINT );
		}
		
		@Override
		protected void handlePress(MouseEvent e) {
			super.handlePress(e);
			DrawingItem item = getItem();			
			DragHandle handle = getHandle();
			if( handle != null && item != null ) {
				int index = (Integer) handle.getData();
				Double p = item.getPoints().get(Math.abs(index-1)); // opposite is the anchor
				anchor = new Double(p.getX(), p.getY());
			}
		}
		
		@Override
		protected void handleRelease(MouseEvent e) {
			super.handleRelease(e);
			anchor = null;
		}

		@Override
		protected void handleDrag(MouseEvent e) {
			DragHandle handle = getHandle();
			DrawingItem item = getItem();
			if( anchor != null && handle != null && handle.getType() == DragHandle.TYPE_POINT ) {
				
				Double p1 = item.getPoints().get(0);
				Double p2 = item.getPoints().get(1);				
				Double p3 = getMapPoint(getInverslyRotatedPoint(e));
				
				if( p3.x < anchor.x )
					p1.x = p3.x;
				else if( p3.x > anchor.x )
					p2.x = p3.x;

				if( p3.y > anchor.y )
					p2.y = p3.y;
				else if( p3.y < anchor.y )
					p1.y = p3.y;
				
				
				item.setPoints(Arrays.asList(new Point2D.Double[] {p1,p2}));	
			}
		}
	}

	public final class RectangleMapEventHandler extends Dragger {

		public RectangleMapEventHandler(Tool tool) {
			super(tool, -1);
		}
		
		@Override
		protected void handlePress(MouseEvent e) {
			DrawingItem item = getItem();
			if( item.getPoints().isEmpty() ) {
				setLastEvent(e);
				setFirstEvent(e);
				setDragging(item);	
				item.addPoint(getMapPoint(e));
				e.consume();
			}
		}

		@Override
		public void handleDrag( MouseEvent e ) {
			DrawingItem item = getItem();
			if( item.getPoints().size() < 2 ) {
				item.addPoint(getMapPoint(e));
			} else {
				Double p1 = item.getPoints().get(0);
				Double p2 = item.getPoints().get(1);
				Double p3 = getMapPoint(e);

				MouseEvent lastEvent = getFirstEvent();
				boolean movingLeft = e.getX() - lastEvent.getX() < 0;
				boolean movingDown = e.getY() - lastEvent.getY() > 0;

				if( p3.x > p2.x )
					p2.x = p3.x;
				else if( p3.x > p1.x ) {
					if( movingLeft )
						p1.x = p3.x;
					else
						p2.x = p3.x;
				} else if( p3.x < p1.x )
					p1.x = p3.x;

				if( p3.y > p2.y )
					p2.y = p3.y;
				else if( p3.y > p1.y ) {
					if( movingDown )
						p1.y = p3.y;
					else
						p2.y = p3.y;
				} else if( p3.y < p1.y )
					p1.y = p3.y;

				item.setPoints(Arrays.asList(new Point2D.Double[] {p1,p2}));
			}
		}
	}
}
