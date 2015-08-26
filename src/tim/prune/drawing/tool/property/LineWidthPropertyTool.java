package tim.prune.drawing.tool.property;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.gui.IconManager;

public class LineWidthPropertyTool extends ListPropertyTool {

	private Map<Integer,JRadioButton> widthCheckboxes;
	
	public LineWidthPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		getToolButton().setIcon(IconManager.getImageIcon("line_width.png"));
	}
	
	@Override
	protected JRadioButton[] getItems() {
		widthCheckboxes = new LinkedHashMap<Integer, JRadioButton>();
		
		for( int i = 1; i <= 25; i++ ) {
			JRadioButton checkbox = new JRadioButton(i+"px");
			checkbox.setActionCommand(String.valueOf(i));
			widthCheckboxes.put(i, checkbox);
		}

		return widthCheckboxes.values().toArray(new JRadioButton[0]);
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		Integer lineWidth = style.getLineWidth();
		if( lineWidth != null ) {
			if( !widthCheckboxes.containsKey(lineWidth) ) {
				lineWidth = widthCheckboxes.size();
			}
			widthCheckboxes.get(lineWidth).setSelected(true);
		}
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		ButtonModel selection = getButtonGroup().getSelection();
		if( selection != null ) {
			String actionCommand = selection.getActionCommand();
			int lineWidth = Integer.parseInt(actionCommand);
			style.setLineWidth(lineWidth);
		}
	}
}
