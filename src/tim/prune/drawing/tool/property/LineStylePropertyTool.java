package tim.prune.drawing.tool.property;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.PathEffect;
import tim.prune.gui.IconManager;

public class LineStylePropertyTool extends ListPropertyTool {

	private Map<PathEffect,JRadioButton> pathCheckboxes;
	
	public LineStylePropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		getToolButton().setIcon(IconManager.getImageIcon("line_style.png"));
	}
	
	@Override
	protected JRadioButton[] getItems() {
		pathCheckboxes = new LinkedHashMap<PathEffect, JRadioButton>();
		
		for( PathEffect pathEffect : PathEffect.values() ) {
			JRadioButton checkbox = new JRadioButton(pathEffect.name());
			checkbox.setActionCommand(pathEffect.name());
			pathCheckboxes.put(pathEffect, checkbox);
		}
		
		return pathCheckboxes.values().toArray(new JRadioButton[0]);
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		if( style.getPathEffect() != null ) {
			pathCheckboxes.get(style.getPathEffect()).setSelected(true);
		}
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		ButtonModel selection = getButtonGroup().getSelection();
		if( selection != null ) {
			String actionCommand = selection.getActionCommand();
			PathEffect pathEffect = PathEffect.valueOf(actionCommand);
			style.setPathEffect(pathEffect);
		}
	}
}
