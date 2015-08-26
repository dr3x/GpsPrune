package tim.prune.drawing.item;

import java.awt.BasicStroke;
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

import tim.prune.KmlDraw;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.PathEffect;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.Projection;

public class Group implements DrawingItem {
	
	private static final BasicStroke LINE_STROKE = new BasicStroke(1, 
			BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, 
			PathEffect.Dash.getPattern(), 0);

	private final List<DrawingItem> items = new ArrayList<DrawingItem>();
	private final Rectangle2D.Double screenRect = new Rectangle2D.Double();
	
	private Color foreground;
	private Color background;
	
	private long id;
	private Drawing drawing;
	private Rectangle2D.Double bounds;


	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setDrawing(Drawing drawing) {
		this.drawing = drawing;
	}

	@Override
	public Drawing getDrawing() {
		return drawing;
	}

	public List<DrawingItem> getItems() {
		return items;
	}
	
	public void setItems( List<DrawingItem> items ) {
		items.clear();
		items.addAll(items);
	}
	
	@Override
	public void setForeground(Color foreground) {
		for( DrawingItem i : items )
			i.setForeground(foreground);
		this.foreground = foreground;
	}

	@Override
	public void setBackground(Color background) {
		for( DrawingItem i : items )
			i.setBackground(background);
		this.background = background;
	}

	@Override
	public Color getBackground() {
		return background;
	}

	@Override
	public Color getForeground() {
		return foreground;
	}
	
	public void setBounds(Rectangle2D.Double bounds) {
		this.bounds = bounds;
	}

	@Override
	public Rectangle2D.Double getBounds() {
		if( bounds == null ) {
			Rectangle2D.Double ret = null;

			for( DrawingItem drawing : items ) {
				List<Point2D.Double> points = drawing.getPoints();
				for( int i = 0; i < points.size(); i++ ) {
					Point2D.Double point = points.get(i);
					if( ret == null )
						ret = new Rectangle2D.Double(point.x, point.y, 0, 0);
					ret.add(point);
				}
			}

			bounds = ret;
		}
		return bounds;
	}

	
	@Override
	public List<Point2D.Double> getPoints() {
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for( DrawingItem i : items )
			points.addAll(i.getPoints());
		return points;
	}

	@Override
	public void setHighlight(Color highlight) {
		for( DrawingItem i : items )
			i.setHighlight(highlight);
	}

	@Override
	public void applyStyle(DrawingStyle style) {
		for( DrawingItem i : items )
			i.applyStyle(style);
	}

	@Override
	public void updateStyle(DrawingStyle style) {
		style.setBackground(background);
		style.setForeground(foreground);
		style.setFontSize(null);
		style.setFontStyle(null);
		style.setLineWidth(null);
		style.setPathEffect(null);
	}

	
	
	@Override
	public DrawingItem hit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		Rectangle2D.Double bounds = getBounds();
		if( bounds == null || screenRect == null ) {
			return null;
		}
		
		return screenRect.contains(x, y) ? this : null;
	}
	
	@Override
	public void draw(MapCanvas canvas, Graphics2D graphics) {
		Rectangle2D.Double bounds = getBounds();		
		
		graphics.setColor(Color.RED);
		graphics.setStroke(LINE_STROKE);
		
		Point2D.Double reuse = new Point2D.Double();
		Projection projection = canvas.getProjection();
		projection.toScreen(bounds.getMinY(), bounds.getMinX(), reuse);
		screenRect.setFrame(reuse.x, reuse.y, 0, 0);
		projection.toScreen(bounds.getMaxY(), bounds.getMaxX(), reuse);
		screenRect.add(reuse);
		
		graphics.draw(screenRect);
	}

	@Override
	public void move(double deltax, double deltay) {
		for( DrawingItem i : items )
			i.move(deltax, deltay);
		
		if( bounds != null ) {
			bounds.x += deltax;
			bounds.y += deltay;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(items.size());
		for( DrawingItem i : items ) {			
			out.writeUTF(i.getClass().getName());
			i.writeExternal(out);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int itemCount = in.readInt();
		for( int i = 0; i < itemCount; i++ ) {
			String type = in.readUTF();
			@SuppressWarnings("unchecked")
			Class<DrawingItem> c = (Class<DrawingItem>) Class.forName(type);
			
			DrawingItem item = null;
			if( drawing == null ) {
				item = KmlDraw.APP.getFactory().createItem(c);
			} else {
				item = drawing.addItem(c);	
			}			
			long itemId = item.getId();
			item.readExternal(in);
			item.setId(itemId);
			items.add(item);
		}
	}
	
	
	
	@Override
	public void addPoint(Point2D.Double point) {
	}
	
	@Override
	public void addPoint(Double point, int index) {	
	}

	@Override
	public void removePoint(Point2D.Double point) {
	}

	@Override
	public void removePoint(int index) {
	}

	@Override
	public void setPoint(Point2D.Double point, int idx) {
	}

	@Override
	public void setPoints(List<Point2D.Double> p) {
	}
}
