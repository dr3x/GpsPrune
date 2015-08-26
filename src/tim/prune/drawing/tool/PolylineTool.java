package tim.prune.drawing.tool;

import java.awt.AWTEvent;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.FreeLine;
import tim.prune.drawing.item.Polyline;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.PointAdder;
import tim.prune.drawing.tool.event.PointDragger;
import tim.prune.drawing.tool.event.Rotator;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.BackgroundPropertyTool;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.LineStylePropertyTool;
import tim.prune.drawing.tool.property.LineWidthPropertyTool;

public class PolylineTool extends Tool {

	private final boolean closed;
	
	public PolylineTool(DrawApp app, boolean closed) {
		super(closed ? "polygon" : "polyline", app);
		this.closed = closed;
		
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new PointDragger(this));
		registerMapEventHandlerDelegate(new Rotator(this));
		registerMapEventHandlerDelegate(new PolylinePointAdder());
		registerMapEventHandlerDelegate(new ToolDeactivator(this, -1));
		
		registerHandleType(DragHandle.TYPE_POINT);
		registerHandleType(DragHandle.TYPE_MOVE);
		registerHandleType(DragHandle.TYPE_ROTATE);
		registerHandleType(DragHandle.TYPE_LINE_MIDPOINT);
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		if( closed )
			registerPropertyToolType(BackgroundPropertyTool.class);
		registerPropertyToolType(LineStylePropertyTool.class);
		registerPropertyToolType(LineWidthPropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		Polyline item = getApp().getDrawing().addItem(Polyline.class);
		item.setClosed(closed);
		return item;
	}

	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Polyline 
			&& !(itemType instanceof FreeLine)
			&& ((Polyline)itemType).isClosed() == this.closed;
	}
	
	private class PolylinePointAdder extends PointAdder {
		public PolylinePointAdder() {
			super(PolylineTool.this, -1);
		}
		
		@Override
		public void handleEvent(AWTEvent e) {
			if( isCreateMode() ) {
				super.handleEvent(e);
			}
		}
	}
}
