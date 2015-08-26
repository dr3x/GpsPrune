package tim.prune.drawing.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.Mutator;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.Projection;

public class Text extends AbstractItem {

	private String text;
	private int fontStyle;
	private int fontSize;
	
	public Text() {
		text = "";
		fontSize = DrawingStyle.DEFAULT_FONT_SIZE;
		fontStyle = DrawingStyle.DEFAULT_FONT_STYLE;
	}
	
	@Mutator(name="add point")
	@Override
	public void addPoint(Double point) {
		super.addPoint(point);
		while( points.size() > 1 ) {
			points.remove(0);
		}
	}
	
	@Mutator(name="add point")
	@Override
	public void addPoint(Double point, int index) {
		super.addPoint(point, index);
		while( points.size() > 1 ) {
			points.remove(0);
		}
	}
	
	@Mutator(name="set points")
	@Override
	public void setPoints(List<Point2D.Double> p) {		
		super.setPoints(p);
		while( points.size() > 1 ) {
			points.remove(0);
		}		
	}
	
	@Mutator(accessor="getText", name="text")
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	@Mutator(accessor="getFontSize", name="font size")
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	
	public int getFontSize() {
		return fontSize;
	}
	
	public int getFontStyle() {
		return fontStyle;
	}
	
	@Mutator(accessor="getFontStyle", name="font style")
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}
	
	@Override
	protected Double getDefaultRotationPoint() {
		return points.size() > 0 ? points.get(0) : null; 
	}
	
	@Override
	public void applyStyle(DrawingStyle style) {
		super.applyStyle(style);
		Integer fontSize = style.getFontSize();
		if( fontSize != null )
			setFontSize(fontSize);
		Integer fontStyle = style.getFontStyle();
		if( fontStyle != null )
			setFontStyle(fontStyle);
	}
	
	@Override
	public void updateStyle(DrawingStyle style) {
		super.updateStyle(style);
		style.setFontSize(getFontSize());
		style.setFontStyle(getFontStyle());
	}
	
	@Override
	public Text doHit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		graphics = prepare(canvas, graphics);
		FontRenderContext frc = graphics.getFontRenderContext();
		Rectangle2D textBound = graphics.getFont().getStringBounds(text, frc);
		boolean hit = graphics.hit(new Rectangle(x, y, 1, 1), textBound, false);
		graphics.dispose();
		return hit ? this : null;
	}
	
	@Override
	public void doDraw(MapCanvas canvas, Graphics2D graphics) {
		if( !points.isEmpty() ) {
			graphics = prepare(canvas, graphics);
			
			Color background = getBackground();
			if( background != null && background.getAlpha() > 0 ) {
				FontRenderContext frc = graphics.getFontRenderContext();
				Rectangle2D textBounds = graphics.getFont().getStringBounds(text, frc);
				graphics.setPaint(background);
				graphics.fill(textBounds);
			}
		
			graphics.setPaint(getForeground());			
			graphics.drawString(text, 0, 0);			
			
			graphics.dispose();
		}
	}
	
	private Graphics2D prepare(MapCanvas canvas, Graphics2D graphics) {
		Projection projection = canvas.getProjection();
		graphics = (Graphics2D) graphics.create();
		
		if( !points.isEmpty() ) {
			Double point = points.get(0);
			Double screen = projection.toScreen(point.y, point.x, null);		
			graphics.translate((int)screen.x, (int)screen.y);
			
			Font font = graphics.getFont();
			graphics.setFont(new Font(font.getName(), fontStyle, fontSize));
		}
		
		return graphics;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(text);
		out.writeInt(fontStyle);
		out.writeInt(fontSize);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		text = in.readUTF();
		fontStyle = in.readInt();
		fontSize = in.readInt();
	}
}
