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
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Text;
import tim.prune.drawing.tool.NumberTool;
import tim.prune.drawing.tool.Tool;
import tim.prune.gui.drawing.OrientableFlowLayout;

public class NumberPropertyTool extends PropertyTool implements ChangeListener {
	
	private final JPanel counterPanel;
	private final JSpinner counterSpinner;
	private final SpinnerNumberModel counterModel;

	public NumberPropertyTool(DrawApp app, JWindow window) {
		super( app, window );
		
		counterPanel = new JPanel(new OrientableFlowLayout());
		
		counterModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
		counterModel.addChangeListener(this);
		
		counterSpinner = new JSpinner(counterModel);
		counterSpinner.setToolTipText("Counter");
		
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(counterSpinner, "'#'#");
		Font font = editor.getTextField().getFont();
		font = new Font(font.getName(), font.getStyle(), 10);
		editor.getTextField().setFont(font);
		editor.getTextField().setColumns(3);
		counterSpinner.setEditor(editor);
		
		counterPanel.add(counterSpinner);
	}
	
	@Override
	public void addTools(JToolBar toolbar) {
		toolbar.add(counterPanel);
	}
	
	@Override
	protected void updateTools(DrawingStyle style) {
		NumberTool tool = null;
		for( Tool t : getApp().getToolbox().getTools() ) {
			if( t instanceof NumberTool ) {
				tool = (NumberTool) t;
				break;
			}
		}
		if( tool != null ) {
			int counter = tool.getCounter();
			counterModel.setValue(counter);
		}
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		NumberTool tool = null;
		for( Tool t : getApp().getToolbox().getTools() ) {
			if( t instanceof NumberTool ) {
				tool = (NumberTool) t;
				break;
			}
		}
		if( tool != null ) {
			tool.setCounter(counterModel.getNumber().intValue());
		}
	}
	
	@Override
	protected void updateItem(DrawingItem item) {
		Text t = (Text) item;
		t.setText(String.valueOf(counterModel.getNumber().intValue()));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateModel();
	}
}
