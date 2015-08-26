package tim.prune.drawing.tool.property;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.DrawApp;
import tim.prune.gui.drawing.SimpleSwatchChooserPanel;

public abstract class ColorPropertyTool extends PopupPropertyTool implements ChangeListener {
	
	private JColorChooser colorChooser;

	public ColorPropertyTool( DrawApp app, JWindow toolPopupWindow ) {
		super( app, toolPopupWindow );
		
		colorChooser = new JColorChooser();
		colorChooser.setChooserPanels(new AbstractColorChooserPanel[] {
				new SimpleSwatchChooserPanel()
		});
		
		colorChooser.getSelectionModel().addChangeListener(this);
	}
	
	@Override
	protected JComponent getPopupContent() {
		return getColorChooser();
	}
	
	protected JColorChooser getColorChooser() {
		return colorChooser;
	}
	
	@Override
	protected AbstractButton buildToolButton() {
		AbstractButton button = super.buildToolButton();
		button.setIcon(createToolIcon());
		return button;
	}
	
	public Icon createToolIcon() {
		BufferedImage image = new BufferedImage(30, 16, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics graphics = image.getGraphics();
		graphics.drawImage(getToolIcon(), 0, 0, null);
		if( colorChooser != null ) {
			Color selectedColor = colorChooser.getSelectionModel().getSelectedColor();
			if( selectedColor != null ) {
				graphics.setColor(selectedColor);
				graphics.fillRect(1, 13, 14, 3);
				graphics.dispose();
			}
		}
		return new ImageIcon(image);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateModel();
		getToolButton().setIcon(createToolIcon());
	}
	
	protected abstract String getToolName();
	protected abstract Image getToolIcon();
}
