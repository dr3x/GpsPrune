package tim.prune.drawing.tool.property;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;

public class FontSizePropertyTool extends ListPropertyTool {

	public FontSizePropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
	}

	@Override
	protected JRadioButton[] getItems() {
		int[] sizes = {
				6, 7, 8, 9, 10, 11, 12, 14, 18, 
				24, 30, 36, 48, 60, 72, 96
		};
		
		JRadioButton[] checkboxes = new JRadioButton[sizes.length];
		for( int i = 0; i < sizes.length; i++ ) {
			checkboxes[i] = new JRadioButton(sizes[i] + "pt");
			checkboxes[i].setActionCommand(String.valueOf(sizes[i]));
		}
		return checkboxes;
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		if( style.getFontSize() != null ) {
			getToolButton().setText(style.getFontSize() + "pt");
		}
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		ButtonGroup group = getButtonGroup();
		ButtonModel selection = group.getSelection();
		if( selection != null ) {
			int size = Integer.parseInt(selection.getActionCommand());
			style.setFontSize(size);			
		}
		updateTools(style);
	}
}
