package tim.prune.drawing.tool;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Group;
import tim.prune.drawing.tool.event.DelegatingMapEventHandler;
import tim.prune.drawing.tool.event.MapEventHandlerDelegate;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.BackgroundPropertyTool;
import tim.prune.drawing.tool.property.FontSizePropertyTool;
import tim.prune.drawing.tool.property.FontStylePropertyTool;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.LineStylePropertyTool;
import tim.prune.drawing.tool.property.LineWidthPropertyTool;

/**
 * Used to edit a group of items
 * 
 * @author mriley
 */
public class GroupTool extends Tool {
	
	public GroupTool(DrawApp app) {
		super( "group", app );
				
		registerMapEventHandlerDelegate(new ItemSelector(this));
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, -1));
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(BackgroundPropertyTool.class);
		registerPropertyToolType(LineStylePropertyTool.class);
		registerPropertyToolType(LineWidthPropertyTool.class);
		registerPropertyToolType(FontSizePropertyTool.class);
		registerPropertyToolType(FontStylePropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		return getApp().getDrawing().addItem(Group.class);
	}

	@Override
	public boolean isToolFor(DrawingItem item) {
		return item instanceof Group;
	}
	
	@Override
	protected void activate() {
		getItem().updateStyle(getApp().getToolbox().getStyle());
		super.activate();
	}
	
	@Override
	protected void deactivate() {
		getApp().setUndoEnabled(false);
		getApp().getDrawing().removeItem(getItem());
		getApp().setUndoEnabled(true);
		super.deactivate();
	}
	
	private final class ItemSelector extends MapEventHandlerDelegate {
		private MouseEvent start;
		
		public ItemSelector(Tool tool) {
			super(tool);
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
			Group item = (Group) getItem();
			if( item == null || !item.getItems().isEmpty() ) {
				return;
			}
			
			Rectangle2D.Double bounds = null;
			switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				start = (MouseEvent) e;
				start.consume();
				break;
			case MouseEvent.MOUSE_MOVED:
			case MouseEvent.MOUSE_DRAGGED:
				if( start != null ) {
					MouseEvent me = (MouseEvent) e;
					Point2D.Double p1 = getMapPoint(start);
					Point2D.Double p2 = getMapPoint(me);
					bounds = new Rectangle2D.Double(p1.x, p1.y, 0, 0);
					bounds.add(p2);					
					item.setBounds(bounds);
					me.consume();
					getHandles().update();
				}
				break;
			case MouseEvent.MOUSE_RELEASED:
				if( start != null ) {
					bounds = item.getBounds();
					for( DrawingItem i : item.getDrawing().getItems() ) {
						for( Point2D.Double p : i.getPoints() ) {
							if( bounds.contains(p) && i != item ) {
								item.getItems().add(i);
								break;
							}
						}
					}
					getHandles().update();
				}
				
				MouseEvent me = (MouseEvent) e;
				start = null;
				me.consume();
				break;
			}	
		}
	}
}
