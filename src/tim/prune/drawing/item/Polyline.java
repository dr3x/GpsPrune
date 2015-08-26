package tim.prune.drawing.item;

import java.awt.BasicStroke;
import java.awt.Color;
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

public class Polyline extends AbstractItem {

	private PathEffect pathEffect;
	private int width;
	private boolean closed;
	
	private Path2D.Double path = new Path2D.Double();
	protected int[] xPoints = new int[0];
	protected int[] yPoints = new int[0];	
	
	public Polyline() {
		this.width = DrawingStyle.DEFAULT_LINE_WIDTH;
		this.pathEffect = DrawingStyle.DEFAULT_PATH_EFFECT;
	}
	
	@Mutator(accessor="getWidth", name="line width")
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	public boolean isClosed() {
		return closed;
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
		xPoints = new int[points.size()];
		yPoints = new int[points.size()];
	}
	
	@Override
	@Mutator(name="add point")
	public void addPoint(Double point, int index) {
		super.addPoint(point, index);
		xPoints = new int[points.size()];
		yPoints = new int[points.size()];
	}
	
	@Override
	@Mutator(name="remove point")
	public void removePoint(int index) {
		super.removePoint(index);
		xPoints = new int[points.size()];
		yPoints = new int[points.size()];
	}
	
	@Override
	@Mutator(name="remove point")
	public void removePoint( Point2D.Double point ) {
		super.removePoint(point);
		xPoints = new int[points.size()];
		yPoints = new int[points.size()];
	}
	
	@Mutator(name="set points")
	@Override
	public void setPoints(List<Point2D.Double> p) {		
		super.setPoints(p);
		xPoints = new int[points.size()];
		yPoints = new int[points.size()];
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
	public Polyline doHit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		if( !path.getBounds().contains(x, y) ) {
			return null;
		}
		
		boolean onStroke = !closed || getBackground().getAlpha() <= 0; 
			
		graphics.setColor(getForeground());
		graphics.setStroke(new BasicStroke(width));
		
		if( closed ) {
			graphics.setBackground(getBackground());
		}
		
		return graphics.hit(new Rectangle(x-width, y-width, width*2, width*2), path, onStroke) ? this : null;
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
		Point2D.Double reuse = new Point2D.Double(); 
		for( int i = 0; i < points.size(); i++ ) {
			projection.toScreen(points.get(i).y, points.get(i).x, reuse);
			xPoints[i] = (int) reuse.x;
			yPoints[i] = (int) reuse.y;
			
			if( i == 0 ) {
				path.moveTo(xPoints[i], yPoints[i]);
			} else {
				path.lineTo(xPoints[i], yPoints[i]);
			}
		}
		
		drawPolyline(graphics, getForeground(), getBackground());
	}

	private void drawPolyline(Graphics2D graphics, Color fg, Color bg) {
		if( points.size() > 1 ) {			
			if( closed ) {
				graphics.setColor(bg);
				graphics.fillPolygon(xPoints, yPoints, points.size());
				graphics.setColor(fg);
				graphics.drawPolygon(xPoints, yPoints, points.size());
			} else {
				graphics.setColor(fg);
				graphics.drawPolyline(xPoints, yPoints, points.size());	
			}
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		width = in.readInt();
		closed = in.readBoolean();
		xPoints = new int[getPoints().size()];
		yPoints = new int[getPoints().size()];
		pathEffect = PathEffect.values()[in.readInt()];
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(width);
		out.writeBoolean(closed);
		out.writeInt(pathEffect.ordinal());
	}
}
