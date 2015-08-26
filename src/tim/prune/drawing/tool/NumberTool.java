package tim.prune.drawing.tool;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Number;
import tim.prune.drawing.item.Text;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.PointAdder;
import tim.prune.drawing.tool.event.Rotator;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.BackgroundPropertyTool;
import tim.prune.drawing.tool.property.FontSizePropertyTool;
import tim.prune.drawing.tool.property.FontStylePropertyTool;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.NumberPropertyTool;

public class NumberTool extends Tool {

	private int counter;

	public NumberTool( DrawApp app ) {
		super("number", app );
		
		registerMapEventHandlerDelegate(new PointAdder(this,1));
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new Rotator(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, -1));
		
		registerHandleType(DragHandle.TYPE_MOVE);
		registerHandleType(DragHandle.TYPE_ROTATE);
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(BackgroundPropertyTool.class);
		registerPropertyToolType(FontSizePropertyTool.class);
		registerPropertyToolType(FontStylePropertyTool.class);
		registerPropertyToolType(NumberPropertyTool.class);
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public int getCounter() {
		return counter;
	}

	@Override
	protected DrawingItem createItem() {
		Text item = getApp().getDrawing().addItem(Number.class);
		item.setText(String.valueOf(++counter));
		return item;
	}

	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Number;
	}
}
