package tim.prune.drawing.tool.property;

import java.awt.Image;

import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.gui.IconManager;

public class ForegroundPropertyTool extends ColorPropertyTool {

	public ForegroundPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
	}

	@Override
	public Image getToolIcon() {
		return IconManager.getImageIcon("pen.png").getImage();
	}
	
	@Override
	public String getToolName() {
		return "Foreground";
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		getColorChooser().getSelectionModel().setSelectedColor(style.getForeground());
		getToolButton().setIcon(createToolIcon());
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		style.setForeground(getColorChooser().getSelectionModel().getSelectedColor());
	}
}
