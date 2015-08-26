package tim.prune.drawing.tool;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Arrow;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.PointAdder;
import tim.prune.drawing.tool.event.PointDragger;
import tim.prune.drawing.tool.event.Rotator;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.LineStylePropertyTool;
import tim.prune.drawing.tool.property.LineWidthPropertyTool;

public class ArrowTool extends Tool {

	public ArrowTool(DrawApp app) {
		super("arrow", app);
		
		registerMapEventHandlerDelegate(new PointAdder(this, 2));
		registerMapEventHandlerDelegate(new PointDragger(this));
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new Rotator(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, -1));
		
		registerHandleType(DragHandle.TYPE_POINT);
		registerHandleType(DragHandle.TYPE_MOVE);
		registerHandleType(DragHandle.TYPE_ROTATE);
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(LineStylePropertyTool.class);
		registerPropertyToolType(LineWidthPropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		return getApp().getDrawing().addItem(Arrow.class);
	}

	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Arrow;
	}
}
