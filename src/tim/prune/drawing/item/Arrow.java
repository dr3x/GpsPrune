package tim.prune.drawing.item;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.Mutator;
import tim.prune.drawing.PathEffect;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.Projection;

public class Arrow extends AbstractItem {

	private PathEffect pathEffect;
	private Path2D.Double path;
	private int width;
	
	public Arrow() {
		this.width = DrawingStyle.DEFAULT_LINE_WIDTH;
		this.pathEffect = DrawingStyle.DEFAULT_PATH_EFFECT;
		this.path = new Path2D.Double();
	}
	
	@Mutator(accessor="getWidth", name="line width")
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getWidth() {
		return width;
	}
	
	@Mutator(accessor="getPathEffect", name="line style")
	public void setPathEffect(PathEffect pathEffect) {
		this.pathEffect = pathEffect;		
	}
	
	public PathEffect getPathEffect() {
		return pathEffect;
	}
	
	@Override
	@Mutator(name="add point")
	public void addPoint( Point2D.Double point ) {
		super.addPoint(point);
		if( points.size() > 2 ) {
			points.remove(1);
		}
	}
	
	@Override
	@Mutator(name="add point")
	public void addPoint(Double point, int index) {
		super.addPoint(point, index);
		if( points.size() > 2 ) {
			points.remove(1);
		}
	}
	
	@Mutator(name="set points")
	@Override
	public void setPoints(List<Point2D.Double> p) {		
		super.setPoints(p);
		while( points.size() > 2 ) {
			points.remove(1);
		}
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
	
	@Override
	public Arrow doHit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		if( !path.getBounds().contains(x, y) ) {
			return null;
		}
			
		graphics.setColor(getForeground());
		graphics.setStroke(new BasicStroke(width));
		return graphics.hit(new Rectangle(x-width, y-width, width*2, width*2), path, true) ? this : null;
	}

	@Override
	public void doDraw(MapCanvas canvas, Graphics2D graphics) {
		Projection projection = canvas.getProjection();
		
		if( pathEffect == null || pathEffect == PathEffect.None ) {
			graphics.setStroke(new BasicStroke(width));
		} else {
			BasicStroke stroke = new BasicStroke(width, 
					BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, 
					pathEffect.getPattern(), 0);
			graphics.setStroke(stroke);
		}
		
		path.reset();
		
		if( points.size() > 1 ) {
			Double screen1 = projection.toScreen(points.get(0).y, points.get(0).x, null);
			Double screen2 = projection.toScreen(points.get(1).y, points.get(1).x, null);
			
			path.moveTo(screen1.x, screen1.y);
			path.lineTo(screen2.x, screen2.y);
			
			int xlen = (int) screen2.distance(screen1);
			graphics = (Graphics2D) graphics.create();
			
			graphics.translate(screen1.x, screen1.y);
			graphics.rotate(Math.atan2(screen2.y - screen1.y, screen2.x - screen1.x));
			
			graphics.setColor(getForeground());
			graphics.drawLine(0,0,xlen,0);
			
			graphics.rotate(Math.toRadians(30), xlen, 0);
			graphics.drawLine((int)(xlen*.80),0,xlen,0);
			
			graphics.rotate(Math.toRadians(-60), xlen, 0);
			graphics.drawLine((int)(xlen*.80),0,xlen,0);
			
			graphics.dispose();
		}
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
