package tim.prune.drawing.tool;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;

public class NullTool extends Tool {

	public NullTool(DrawApp app) {
		super("null", app);
	}
	
	@Override
	protected DrawingItem createItem() {
		return null;
	}

	@Override
	public boolean isToolFor(DrawingItem item) {
		return false;
	}
}
