package tim.prune.drawing.tool;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.FreeLine;
import tim.prune.drawing.tool.event.DelegatingMapEventHandler;
import tim.prune.drawing.tool.event.MapEventHandlerDelegate;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.LineStylePropertyTool;
import tim.prune.drawing.tool.property.LineWidthPropertyTool;

public class FreeLineTool extends Tool {

	public FreeLineTool(DrawApp app) {
		super( "freehand", app );
		
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new FreeLineEventHandler(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, 1));
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(LineStylePropertyTool.class);
		registerPropertyToolType(LineWidthPropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		return getApp().getDrawing().addItem(FreeLine.class);
	}

	@Override
	public boolean isToolFor(DrawingItem item) {
		return item instanceof FreeLine;
	}


	public final class FreeLineEventHandler extends MapEventHandlerDelegate {

		private MouseEvent start;
		private boolean startNewLine;
		
		public FreeLineEventHandler(Tool tool) {
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
			switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				if( startNewLine ) {
					DrawApp app = getApp();
					Toolbox toolbox = app.getToolbox();
					toolbox.reactivate();
				}
				start = (MouseEvent) e;
				start.consume();
				break;
			case MouseEvent.MOUSE_MOVED:
			case MouseEvent.MOUSE_DRAGGED:
				if( start != null ) {
					MouseEvent me = (MouseEvent) e;
					getItem().addPoint(getMapPoint(me));
					me.consume();
				}
				break;
			case MouseEvent.MOUSE_RELEASED:
				MouseEvent me = (MouseEvent) e;
				start = null;
				startNewLine = true;
				me.consume();
				break;
			}
		}
	}
}
