package tim.prune.drawing.tool;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.JWindow;

import tim.prune.drawing.item.Text;

public class TextToolWindow extends JWindow implements KeyListener {
	private static final long serialVersionUID = -6352739362762069994L;

	private final Text text;
	private final JTextField textField;
	
	public TextToolWindow(Frame frame, Text t) {
		super(frame);
		setAlwaysOnTop(true);
		
		text = t;
		textField = new JTextField(10);
		textField.setOpaque(false);
		textField.addKeyListener(this);
		setLayout(new BorderLayout());
		add(textField, BorderLayout.CENTER);
		
		update();
	}
	
	public void update() {
		textField.setText(text.getText());
		Font f = textField.getFont();
		f = new Font(f.getName(), text.getFontStyle(), text.getFontSize());
		textField.setFont(f);
		textField.setForeground(text.getForeground());
		textField.setBackground(text.getBackground());
		textField.setColumns(text.getText().length()+1);
		
		setBackground(text.getBackground());
		pack();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		text.setText(textField.getText());
	}
	
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
