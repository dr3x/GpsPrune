package tim.prune.drawing.tool;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Ellipse;

public class EllipseTool extends RectangleTool {

	public EllipseTool(DrawApp app) {
		super( "ellipse", app );
	}
	
	@Override
	protected DrawingItem createItem() {
		Ellipse item = getApp().getDrawing().addItem(Ellipse.class);
		return item;
	}
	
	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Ellipse;
	}
}
