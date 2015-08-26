package tim.prune.drawing.item;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.Mutator;
import tim.prune.drawing.PathEffect;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.Projection;

public class Rectangle extends AbstractItem {
	
	private int width;
	private PathEffect pathEffect;
	private RectangularShape shape;
	private Point2D.Double tmpPoint1;
	private Point2D.Double tmpPoint2;
	
	public Rectangle() {
		shape = createShape();
		pathEffect = DrawingStyle.DEFAULT_PATH_EFFECT;
		width = DrawingStyle.DEFAULT_LINE_WIDTH;
		tmpPoint1 = new Point2D.Double();
		tmpPoint2 = new Point2D.Double();
	}
	
	protected RectangularShape createShape() {
		return new Rectangle2D.Double();
	}

	public int getWidth() {
		return width;
	}
	
	@Mutator(accessor="getWidth", name="line width")
	public void setWidth(int width) {
		this.width = width;
	}
	
	@Mutator(accessor="getPathEffect", name="line style")
	public void setPathEffect(PathEffect pathEffect) {
		this.pathEffect = pathEffect;
	}
	
	public PathEffect getPathEffect() {
		return pathEffect;
	}
	
	@Mutator(name="add point")
	@Override
	public void addPoint(Double point) {
		super.addPoint(point);
		while( points.size() > 2 ) {
			points.remove(0);
		}
		fixPoints();
	}
	
	@Mutator(name="add point")
	@Override
	public void addPoint(Double point, int index) {
		super.addPoint(point, index);
		while( points.size() > 2 ) {
			points.remove(0);
		}
		fixPoints();
	}
	
	@Mutator(name="set point")
	@Override
	public void setPoint(Double point, int idx) {		
		super.setPoint(point, idx);
		fixPoints();
	}
	
	@Mutator(name="set points")
	@Override
	public void setPoints(List<Point2D.Double> p) {		
		super.setPoints(p);
		fixPoints();
	}
	
	@Override
	public void applyStyle(DrawingStyle style) {
		super.applyStyle(style);
		Integer lineWidth = style.getLineWidth();
		if( lineWidth != null )
			setWidth(lineWidth);
		PathEffect pathEffect = style.getPathEffect();
		if( pathEffect != null )
			setPathEffect(pathEffect);
	}
	
	@Override
	public void updateStyle(DrawingStyle style) {
		super.updateStyle(style);
		style.setLineWidth(getWidth());
		style.setPathEffect(getPathEffect());
	}
	
	private void fixPoints() {
		if( points.size() == 2 ) {
			Double p1 = points.get(0);
			Double p2 = points.get(1);
			
			double minx = Math.min(p1.x, p2.x);
			double maxx = Math.max(p1.x, p2.x);
			double miny = Math.min(p1.y, p2.y);
			double maxy = Math.max(p1.y, p2.y);
			
			p1.x = minx;
			p1.y = miny;
			p2.x = maxx;
			p2.y = maxy;
		}
	}
	
	@Override
	public void doDraw(MapCanvas canvas, Graphics2D graphics) {
		if( points.size() != 2 ) {
			return;
		}
		
		Projection projection = canvas.getProjection();

		Point2D.Double sw = points.get(0);
		Point2D.Double ne = points.get(1);
		Point2D.Double swscreen = projection.toScreen(sw.y, sw.x, tmpPoint1);
		Point2D.Double nescreen = projection.toScreen(ne.y, ne.x, tmpPoint2);
		
		double width = Math.abs(nescreen.x - swscreen.x);
		double height = Math.abs(nescreen.y - swscreen.y);
		shape.setFrame(swscreen.x, nescreen.y, width, height);
		
		if( pathEffect == null || pathEffect == PathEffect.None ) {
			graphics.setStroke(new BasicStroke(this.width));
		} else {
			BasicStroke stroke = new BasicStroke(this.width, 
					BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, 
					pathEffect.getPattern(), 0);
			graphics.setStroke(stroke);
		}
		
		Color background = getBackground();
		if( background != null && background.getAlpha() > 0 ) {
			graphics.setColor(background);
			graphics.fill(shape);
		}
		
		graphics.setColor(getForeground());
		graphics.draw(shape);
	}

	@Override
	public Rectangle doHit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		graphics.setColor(getForeground());
		graphics.setStroke(new BasicStroke(width));
		return graphics.hit(new java.awt.Rectangle(x-width, y-width, width*2, width*2), 
				shape, getBackground().getAlpha() <= 0) ? this : null;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		width = in.readInt();
		pathEffect = PathEffect.values()[in.readInt()];
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(width);
		out.writeInt(pathEffect.ordinal());
	}
}
