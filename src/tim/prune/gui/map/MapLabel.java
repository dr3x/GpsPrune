package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class MapLabel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final int align;

	public MapLabel(int align) {
		this.align = align;
		setOpaque(false);
		setPreferredSize(new Dimension(150, 35));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		String text = getText();
		int startX = 0;
		if( text.trim().length() > 0 ) {
			if( align == SwingConstants.CENTER ) {
				startX = getWidth() / 2 - g.getFontMetrics().stringWidth(text) / 2;
			} else if( align == SwingConstants.RIGHT ) {
				startX = getWidth() - g.getFontMetrics().stringWidth(text) - 10;
			}
		}

		int y = 11;
		g.setColor(Color.BLACK);
		g.drawString(text, startX-1, y);
		g.drawString(text, startX+1, y);
		g.drawString(text, startX, y-1);
		g.drawString(text, startX, y+1);
		g.setColor(Color.WHITE);
		g.drawString(text, startX, y);
	}

	protected abstract String getText();
}
