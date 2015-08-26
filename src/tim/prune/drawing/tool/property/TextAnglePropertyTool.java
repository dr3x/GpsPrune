package tim.prune.drawing.tool.property;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.gui.drawing.OrientableFlowLayout;

public class TextAnglePropertyTool extends PropertyTool implements ChangeListener {

	private final JPanel angleContainer;
	private final JSpinner angle;
	private final SpinnerNumberModel angleModel;
	
	public TextAnglePropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		
		angleContainer = new JPanel(new OrientableFlowLayout());
		
		angleModel = new SpinnerNumberModel(0, 0, 360, 1);
		angleModel.addChangeListener(this);
				
		angle = new JSpinner(angleModel);
		angle.setToolTipText("Text Rotation");
		
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(angle, "#'\u00b0'");
		Font font = editor.getTextField().getFont();
		font = new Font(font.getName(), font.getStyle(), 10);
		editor.getTextField().setFont(font);
		editor.getTextField().setColumns(3);
		angle.setEditor(editor);
		
		angleContainer.add(angle);
	}
	
	@Override
	public void addTools(JToolBar toolbar) {
		toolbar.add(angleContainer);
	}

	@Override
	protected void updateTools(DrawingStyle style) {
//		angleModel.setValue(style.getTextRotation());
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
//		style.setTextRotation(angleModel.getNumber().intValue());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateModel();
	}
}
