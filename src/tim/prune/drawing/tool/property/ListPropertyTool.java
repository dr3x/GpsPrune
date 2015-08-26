package tim.prune.drawing.tool.property;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JWindow;

import tim.prune.DrawApp;

public abstract class ListPropertyTool extends PopupPropertyTool implements ItemListener {

	private ButtonGroup buttonGroup;
	private JPanel popup;
	
	public ListPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		
		buttonGroup = new ButtonGroup();
		popup = new JPanel();
		popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
		for( JRadioButton item : getItems() ) {
			item.addItemListener(this);
			popup.add(item);
			buttonGroup.add(item);
		}
	}
	
	@Override
	protected JComponent getPopupContent() {
		return popup;
	}
	
	protected ButtonGroup getButtonGroup() {
		return buttonGroup;
	}
	
	protected abstract JRadioButton[] getItems();
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		updateModel();
		hidePopup();
	}
}
