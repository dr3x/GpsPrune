package tim.prune.drawing.tool.property;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingStyle;
import tim.prune.gui.IconManager;

public class FontStylePropertyTool extends PropertyTool implements ActionListener {

	private final JPanel buttonPanel;
	private final JToggleButton boldButton;
	private final JToggleButton italicButton;
	
	public FontStylePropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		
		buttonPanel = new JPanel(new GridLayout(1, 2, 2, 0));
		
		boldButton = new JToggleButton(IconManager.getImageIcon("bold.png"));
		italicButton = new JToggleButton(IconManager.getImageIcon("italic.png"));
		boldButton.setPreferredSize(new Dimension(28, 28));
		italicButton.setPreferredSize(new Dimension(28, 28));		
		boldButton.setToolTipText("Bold");
		italicButton.setToolTipText("Italic");
		boldButton.setBorderPainted(false);
		italicButton.setBorderPainted(false);
		boldButton.setBackground(buttonPanel.getBackground());
		boldButton.addActionListener(this);
		italicButton.addActionListener(this);
		
		buttonPanel.add(boldButton);
		buttonPanel.add(italicButton);
		
	}
	
	@Override
	public void addTools(JToolBar toolbar) {
		toolbar.add(boldButton);
		toolbar.add(italicButton);
	}

	@Override
	protected void updateTools(DrawingStyle style) {
		Integer fontStyle = style.getFontStyle();
		if( fontStyle != null ) {
			boldButton.setSelected((fontStyle & Font.BOLD) == Font.BOLD);
			italicButton.setSelected((fontStyle & Font.ITALIC) == Font.ITALIC);
		}
	}

	@Override
	protected void updateStyle(DrawingStyle style) {
		int newStyle = 0;
		newStyle |= (boldButton.isSelected() ? Font.BOLD : 0);
		newStyle |= (italicButton.isSelected() ? Font.ITALIC : 0);
		style.setFontStyle(newStyle);
		updateTools(style);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateModel();
	}
}
