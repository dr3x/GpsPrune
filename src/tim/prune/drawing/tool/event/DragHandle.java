package tim.prune.drawing.tool.event;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.NumberFormat;

import tim.prune.draw.Drawable;
import tim.prune.draw.Hitable;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.Projection;

public class DragHandle implements Drawable, Hitable {
	
	private static final NumberFormat numberFormat = NumberFormat.getNumberInstance();
	static {
		numberFormat.setMaximumFractionDigits(5);		
	}
	
	public static final int TYPE_DRAWING = 0x001;
	public static final int TYPE_MOVE = 0x002;
	public static final int TYPE_POINT = 0x004;
	public static final int TYPE_ROTATE = 0x008;
	public static final int TYPE_LINE_MIDPOINT = 0x010;
	
	private static final int SIZE = 8;
	private static final int HSIZE = SIZE/2;
	
	private int type;
	private RectangularShape shape;
	private Point2D.Double point;
	private Point2D.Double pixelOffset;
	private double rotationAngle;
	private Point2D.Double rotationPoint;
	private Object data;
	
	public DragHandle(int type, Point2D.Double point, Point2D.Double pixelOffset, Object data) {
		this.type = type;
		switch (type) {
		case TYPE_ROTATE:
		case TYPE_MOVE:
			shape = new Ellipse2D.Double(0, 0, SIZE, SIZE);
			break;		
		default:
			shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
		}
		
		setPoint(point);
		setData(data);
		setPixelOffset(pixelOffset);
	}
	
	public DragHandle(int type, Point2D.Double point, Object data) {
		this( type, point, new Point2D.Double(), data);
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public void setPoint(Point2D.Double point) {
		this.point = point;
	}
	
	public Point2D.Double getPixelOffset() {
		return pixelOffset;
	}
	
	public void setPixelOffset(Point2D.Double pixelOffset) {
		this.pixelOffset = pixelOffset;
	}
	
	public void setRotation(double rotationAngle, Point2D.Double rotationPoint) {
		this.rotationAngle = rotationAngle;
		this.rotationPoint = rotationPoint;
	}

	@Override
	public DragHandle hit(MapCanvas canvas, Graphics2D graphics, int x, int y) {
		Projection projection = canvas.getProjection();
		Graphics2D backup = graphics;
		if( rotationAngle != 0 && rotationPoint != null ) {
			graphics = (Graphics2D) graphics.create();
			Double rotationScreen = projection.toScreen(rotationPoint.y, rotationPoint.x, null);
			graphics.rotate(Math.toRadians(rotationAngle), rotationScreen.x, rotationScreen.y);
		}
		DragHandle ret = graphics.hit(new Rectangle(x, y, 1, 1), shape, false) ? this : null;
		if( backup != graphics ) {
			graphics.dispose();
		}
		return ret;
	}
	
	@Override
	public void draw(MapCanvas canvas, Graphics2D graphics) {
		Projection projection = canvas.getProjection();
		
		Color fill = null;
		Color stroke = null;
		
		switch (type) {
		case TYPE_MOVE:
			stroke = Color.BLACK;
			fill = Color.BLUE;
			break;
		case TYPE_ROTATE:
			stroke = Color.BLACK;
			fill = Color.GREEN;
			break;
		case TYPE_LINE_MIDPOINT:
			stroke = Color.BLACK;
			fill = Color.WHITE;
			fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 0x99);
			break;
		default:
			stroke = Color.BLACK;
			fill = Color.WHITE;
			break;
		}
		
		Double screen = projection.toScreen(point.y, point.x, null);
		Graphics2D backup = graphics;
		if( rotationAngle != 0 && rotationPoint != null ) {
			graphics = (Graphics2D) graphics.create();
			Double rotationScreen = projection.toScreen(rotationPoint.y, rotationPoint.x, null);
			graphics.rotate(Math.toRadians(rotationAngle), rotationScreen.x, rotationScreen.y);
		}
		shape.setFrame(pixelOffset.x+screen.x-HSIZE, pixelOffset.y+screen.y-HSIZE, SIZE, SIZE);
		
		graphics.setStroke(new BasicStroke(.5f));
		graphics.setColor(fill);
		graphics.fill(shape);
		graphics.setColor(stroke);
		graphics.draw(shape);
		
		if( backup != graphics ) {
			graphics.dispose();
		}
	}
}
