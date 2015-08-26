package tim.prune.drawing.tool.property;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.item.Text;
import tim.prune.gui.drawing.OrientableFlowLayout;

public class TextPropertyTool extends PropertyTool implements KeyListener {

	private JPanel textContainer;
	private JTextField text;
	
	public TextPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		
		textContainer = new JPanel(new OrientableFlowLayout());
		
		text = new JTextField(10);
		text.addKeyListener(this);
		
		Font font = text.getFont();
		font = new Font(font.getName(), font.getStyle(), 10);
		text.setFont(font);
		
		textContainer.add(text);
	}
	
	@Override
	public void addTools(JToolBar toolbar) {
		toolbar.add(textContainer);
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		text.setText(((Text)getItem()).getText());
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		((Text)getItem()).setText(text.getText());
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		updateModel();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {	
	}
}
