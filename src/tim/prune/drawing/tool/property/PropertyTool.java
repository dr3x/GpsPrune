package tim.prune.drawing.tool.property;

import javax.swing.JToolBar;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.DrawingItem;

public abstract class PropertyTool {
	
	private DrawApp app;
	private DrawingItem item;
	private boolean updateEnabled = true;
	private JWindow toolPopupWindow;
	
	public PropertyTool(DrawApp app, JWindow toolPopupWindow) {
		this.app = app;
		this.toolPopupWindow = toolPopupWindow;		
	}

	public void setItem(DrawingItem item) {
		this.item = item;
		setUpdateEnabled(false);
		DrawingStyle style = new DrawingStyle();
		item.updateStyle(style);
		updateTools(style);
		setUpdateEnabled(true);
	}
	
	public JWindow getToolPopupWindow() {
		return toolPopupWindow;
	}

	public DrawingItem getItem() {
		return item;
	}

	public DrawApp getApp() {
		return app;
	}
	
	public DrawingStyle getStyle() {
		return app.getToolbox().getStyle();
	}

	protected void setUpdateEnabled(boolean updateEnabled) {
		this.updateEnabled = updateEnabled;
	}

	protected void updateModel() {
		if( updateEnabled ) {
			DrawingStyle style = app.getToolbox().getStyle();
			updateStyle(style);
			updateItem(item);
			item.applyStyle(style);
			app.getCanvas().repaint();
		}
	}	

	protected void updateItem( DrawingItem item ) {}
	protected abstract void updateTools( DrawingStyle style );	
	protected abstract void updateStyle( DrawingStyle style );
	public abstract void addTools(JToolBar toolbar);
}
