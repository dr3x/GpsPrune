package tim.prune.drawing.tool.property;

import java.awt.Image;

import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.gui.IconManager;

public class BackgroundPropertyTool extends ColorPropertyTool {

	public BackgroundPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
	}

	@Override
	public Image getToolIcon() {
		return IconManager.getImageIcon("bucket.png").getImage();
	}
	
	@Override
	public String getToolName() {
		return "Background";
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		getColorChooser().getSelectionModel().setSelectedColor(style.getBackground());
		getToolButton().setIcon(createToolIcon());
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		style.setBackground(getColorChooser().getSelectionModel().getSelectedColor());
	}
}
