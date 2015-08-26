package tim.prune.drawing;

import java.awt.Color;
import java.awt.Font;

public class DrawingStyle {
	
	public static final Color DEFAULT_BACKGROUND = new Color(0, 0, 0, 0);
	public static final Color DEFAULT_FOREGROUND = Color.BLUE;
	public static final int DEFAULT_FONT_SIZE = 14;
	public static final int DEFAULT_FONT_STYLE = Font.PLAIN;
	public static final int DEFAULT_LINE_WIDTH = 3;
	public static final PathEffect DEFAULT_PATH_EFFECT = PathEffect.None;

	private Color foreground;
	private Color background;
	private Integer fontStyle;
	private Integer fontSize;
	private Integer lineWidth;
	private PathEffect pathEffect;
	
	public DrawingStyle() {
		foreground = DEFAULT_FOREGROUND;
		background = DEFAULT_BACKGROUND;
		fontSize = DEFAULT_FONT_SIZE;
		fontStyle = DEFAULT_FONT_STYLE;
		lineWidth = DEFAULT_LINE_WIDTH;
		pathEffect = DEFAULT_PATH_EFFECT;
	}
	
	public Color getForeground() {
		return foreground;
	}
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}
	public Color getBackground() {
		return background;
	}
	public void setBackground(Color background) {
		this.background = background;
	}
	public Integer getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(Integer fontStyle) {
		this.fontStyle = fontStyle;
	}
	public Integer getFontSize() {
		return fontSize;
	}
	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}
	public Integer getLineWidth() {
		return lineWidth;
	}
	public void setLineWidth(Integer lineWidth) {
		this.lineWidth = lineWidth;
	}
	public void setPathEffect(PathEffect pathEffect) {
		this.pathEffect = pathEffect;
	}
	public PathEffect getPathEffect() {
		return pathEffect;
	}
}
