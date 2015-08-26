package tim.prune.drawing.item;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import tim.prune.draw.Rotatable;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.Mutator;
import tim.prune.gui.map.MapCanvas;

public abstract class AbstractItem implements DrawingItem, Rotatable {
	
	protected final List<Point2D.Double> points = new ArrayList<Point2D.Double>();
	
	private long id;
	
	private Color foreground;
	private Color background;
	
	private Color highlight;
	private Color backgroundHighlight;
	private boolean enableHighlight;
	
	private Drawing drawing;
	
	private double rotationAngle;
	private Double rotationPoint;
	
	public AbstractItem() {
		foreground = DrawingStyle.DEFAULT_FOREGROUND;
		background = DrawingStyle.DEFAULT_BACKGROUND;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setId(long)
	 */
	@Override
	public void setId(long id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#getId()
	 */
	@Override
	public long getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setDrawing(tim.prune.drawing.Drawing)
	 */
	@Override
	public void setDrawing(Drawing drawing) {
		this.drawing = drawing;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		return drawing;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setForeground(java.awt.Color)
	 */
	@Override
	@Mutator(accessor="getForeground", name="foreground color")
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setBackground(java.awt.Color)
	 */
	@Override
	@Mutator(accessor="getBackground", name="background color")
	public void setBackground(Color background) {
		this.background = background;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#getBackground()
	 */
	@Override
	public Color getBackground() {
		return enableHighlight && background != null && background.getAlpha() > 0 ? 
				backgroundHighlight : background;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#getForeground()
	 */
	@Override
	public Color getForeground() {
		return enableHighlight ? highlight : foreground;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#addPoint(java.awt.geom.Point2D.Double)
	 */
	@Override
	@Mutator(name="add point")
	public void addPoint( Point2D.Double point ) {
		points.add(point);
	}
	
	@Override
	@Mutator(name="add point")
	public void addPoint(Double point, int index) {
		points.add(index, point);
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#removePoint(java.awt.geom.Point2D.Double)
	 */
	@Override
	@Mutator(name="remove point")
	public void removePoint( Point2D.Double point ) {
		points.remove(point);
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#removePoint(int)
	 */
	@Override
	@Mutator(name="remove point")
	public void removePoint( int index ) {
		if( index > -1 && index < points.size() ) {
			points.remove(index);
		}
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setPoint(java.awt.geom.Point2D.Double, int)
	 */
	@Override
	@Mutator(name="set point")
	public void setPoint( Point2D.Double point, int idx ) {
		points.set(idx, point);
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setPoints(java.util.List)
	 */
	@Override
	@Mutator(name="set points")
	public void setPoints( List<Point2D.Double> p ) {
		if( p != points ) {
			points.clear();
			points.addAll(p);
		}
	}
	
	@Override
	@Mutator(name="move")
	public void move( double deltax, double deltay ) {
		for( int i = 0; i < points.size(); i++ ) {
			Point2D.Double p = points.get(i);
			p.x += deltax;
			p.y += deltay;
		}
	}
	
	@Override
	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double ret = null;
		for( int i = 0; i < points.size(); i++ ) {
			Point2D.Double point = points.get(i);
			if( ret == null )
				ret = new Rectangle2D.Double(point.x, point.y, 0, 0);
			ret.add(point);
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#getPoints()
	 */
	@Override
	public List<Point2D.Double> getPoints() {
		return points;
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#setHighlight(java.awt.Color)
	 */
	@Override
	public void setHighlight(Color highlight) {
		this.highlight = highlight;
		if( highlight != null ) {
			this.backgroundHighlight = new Color(highlight.getRed(), 
					highlight.getGreen(), highlight.getBlue(), 
					background.getAlpha());
		} else {
			this.backgroundHighlight = null;
		}
	}
	
	@Override
	public double getRotationAngle() {
		return rotationAngle;
	}
	
	@Override
	public Double getRotationPoint() {
		return rotationPoint;
	}
	
	@Override
	@Mutator(accessor="getRotationAngle", name="rotate")
	public void rotate(double angle, Double point) {
		rotationAngle = angle;
		rotationPoint = point;
		if( angle != 0 && rotationPoint == null ) {
			rotationPoint = getDefaultRotationPoint();
		}
	}
	
	protected Double getDefaultRotationPoint() {
		if( points.size() > 0 ) {
			Rectangle2D.Double bounds = getBounds();
			return new Double(bounds.getCenterX(), bounds.getCenterY());
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#applyStyle(tim.prune.drawing.DrawingStyle)
	 */
	@Override
	public void applyStyle( DrawingStyle style ) {
		Color c = style.getForeground();
		if( c != null )
			setForeground(c);
		
		c = style.getBackground();
		if( c != null )
			setBackground(c);
	}
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#updateStyle(tim.prune.drawing.DrawingStyle)
	 */
	@Override
	public void updateStyle( DrawingStyle style ) {
		style.setForeground(getForeground());
		style.setBackground(getBackground());
	}
	
	@Override
	public final void draw(MapCanvas canvas, Graphics2D graphics) {
		Graphics2D tmpGraphics = null;
		if( rotationAngle != 0 && rotationPoint != null ) {
			Double screen = canvas.getProjection().toScreen(rotationPoint.y, rotationPoint.x, null);			
			tmpGraphics = (Graphics2D) graphics.create();
			graphics = tmpGraphics;
			graphics.rotate(Math.toRadians(rotationAngle), screen.x, screen.y);
		}
		
		doDraw(canvas, graphics);
		
		if( highlight != null ) {
			enableHighlight = true;
			doDraw(canvas, graphics);
			enableHighlight = false;
		}
		
		if( tmpGraphics != null ) {
			tmpGraphics.dispose();
		}
	}
	
	protected abstract void doDraw(MapCanvas canvas, Graphics2D graphics);

	
	@Override
	public final AbstractItem hit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		Graphics2D tmpGraphics = null;
		try {
			if( rotationAngle != 0 && rotationPoint != null ) {
				Double screen = canvas.getProjection().toScreen(rotationPoint.y, rotationPoint.x, null);			
				tmpGraphics = (Graphics2D) graphics.create();
				graphics = tmpGraphics;
				graphics.rotate(Math.toRadians(rotationAngle), screen.x, screen.y);
			}

			return doHit(canvas, graphics, x, y);
		} finally {
			if( tmpGraphics != null ) {
				tmpGraphics.dispose();
			}
		}
	}
	
	protected abstract AbstractItem doHit(MapCanvas canvas, Graphics2D graphics, int x, int y);
	
	/* (non-Javadoc)
	 * @see tim.prune.drawing.DrawingItem#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( !(obj instanceof AbstractItem) )
			return false;
		return ((AbstractItem) obj).id == id;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readUTF(); // skip class name
		id = in.readLong();
		foreground = readColor(in);
		background = readColor(in);
		
		points.clear();
		int numPoints = in.readInt();
		for( int i = 0; i < numPoints; i++ ) {
			points.add(new Point2D.Double(in.readDouble(), in.readDouble()));
		}
		rotationAngle = in.readDouble();
		if( rotationAngle != 0 ) {
			rotate(rotationAngle, null);
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if( Enhancer.isEnhanced(getClass()) ) {
			out.writeUTF(getClass().getSuperclass().getName());	
		} else {
			out.writeUTF(getClass().getName());
		}
		
		out.writeLong(id);
		writeColor(out, foreground);
		writeColor(out, background);
		
		out.writeInt(points.size());
		for( Point2D.Double d : points ) {
			out.writeDouble(d.x);
			out.writeDouble(d.y);
		}
		out.writeDouble(rotationAngle);
	}
	
	protected static Color readColor(ObjectInput in) throws IOException {
		int r = in.readInt();
		int g = in.readInt();
		int b = in.readInt();
		int a = in.readInt();
		return new Color(r, g, b, a);
	}

	protected static void writeColor(ObjectOutput out, Color color) throws IOException {
		if( color == null ) {
			out.writeInt(0);
			out.writeInt(0);
			out.writeInt(0);
			out.writeInt(0);
		} else {
			out.writeInt(color.getRed());
			out.writeInt(color.getGreen());
			out.writeInt(color.getBlue());
			out.writeInt(color.getAlpha());			
		}
	}
}
