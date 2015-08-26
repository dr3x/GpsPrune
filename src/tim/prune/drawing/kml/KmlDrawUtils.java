package tim.prune.drawing.kml;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.PathEffect;
import tim.prune.drawing.item.Arrow;
import tim.prune.drawing.item.Ellipse;
import tim.prune.drawing.item.FreeLine;
import tim.prune.drawing.item.Polyline;
import tim.prune.drawing.item.Rectangle;

public final class KmlDrawUtils {

	public static final String ELEM_DRAWING_ITEM = "DrawingItem";
	public static final String ELEM_CIRCLE = "Circle";
	public static final String ELEM_POINT = "Point";
	public static final String ELEM_POLYGON = "Polygon";
	public static final String ELEM_SQUARE = "Square";
	public static final String ELEM_RECTANGLE = "Rectangle";
	public static final String ELEM_ARROW = "Arrow";
	public static final String ELEM_LINE_STRING = "LineString";
	public static final String ELEM_LABEL_STYLE = "LabelStyle";
	public static final String ELEM_POINT_STYLE = "PointStyle";
	public static final String ELEM_ARC_STYLE = "ArcStyle";
	public static final String ELEM_AXIS_STYLE = "AxisStyle";
	public static final String ELEM_POLY_STYLE = "PolyStyle";
	public static final String ELEM_LINE_STYLE = "LineStyle";
	public static final String ELEM_COLOR = "color";
	public static final String ELEM_BG_COLOR = "backgroundColor";
	public static final String ELEM_LINE_STYLES = "lineStyles";
	public static final String ELEM_TEXT_SIZE = "textSize";
	public static final String ELEM_FONT_SIZE = "fontSize";
	public static final String ELEM_SCALE = "scale";
	public static final String ELEM_WIDTH = "width";
	public static final String ELEM_COORDINATES = "coordinates";
	public static final String ELEM_PLACEMARK_NAME = "name";
	public static final String ELEM_PLACEMARK = "Placemark";
	public static final String ELEM_FONT_STYLE = "fontStyle";
	public static final String ELEM_ANGLE = "angle";
	public static final String ELEM_SEMI_MAJOR_AXIS = "semiMajorAxis";
	public static final String ELEM_SEMI_MINOR_AXIS = "semiMinorAxis";
	public static final String ELEM_EXT_DATA = "ExtendedData";
	public static final String ELEM_DATA = "Data";
	public static final String ATTR_DATA_NAME = "name";
	public static final String ATTR_DATA_VALUE = "value";
	public static final String KEY_DATA_NAME_TYPE = "KmlDrawType";
	public static final String KEY_DATA_NAME_BOUNDS = "BoundingBox";
	public static final String KEY_DATA_NAME_WIDTH = "Width";
	public static final String KEY_DATA_NAME_LINE_STYLE = "LineStyle";
	public static final String KEY_DATA_NAME_BACKGROUND = "BackgroundColor";
	public static final String KEY_DATA_NAME_TOPLEFT = "TopLeft";
	public static final String KEY_DATA_NAME_BOTTOMRIGHT = "BottomRight";
	public static final String KEY_DATA_NAME_DRAWINGTYPE = "DrawingType";
	public static final String KEY_DATA_NAME_ANGLE = "Angle";
	public static final String KEY_DATA_NAME_ROT_POINT = "RotationPoint";
	public static final String ELEM_DATA_VAL = "value";
	

	
	public static String coordsToStringReverse(Rectangle2D.Double points) {
		return coordsToStringReverse(Arrays.asList(
				new Point2D.Double(points.x, points.y),
				new Point2D.Double(points.x, points.y+points.height),
				new Point2D.Double(points.x+points.width, points.y+points.height),
				new Point2D.Double(points.x+points.width, points.y)
		));
	}
	
	public static String coordsToStringReverse(Point2D.Double point) {
		return coordsToStringReverse(Collections.singletonList(point));
	}
	
	public static String coordsToStringReverse(List<Point2D.Double> points) {		
		StringBuilder list = new StringBuilder();
		for( Point2D.Double point : points ) {		
			if( list.length() > 0 ) list.append(" ");
			list.append(point.y).append(",").append(point.x).append(",").append(0);
		}		
		return list.toString();
	}
	
	public static String coordsToString(Rectangle2D.Double points) {
		return coordsToString(Arrays.asList(
				new Point2D.Double(points.x, points.y),
				new Point2D.Double(points.x, points.y+points.height),
				new Point2D.Double(points.x+points.width, points.y+points.height),
				new Point2D.Double(points.x+points.width, points.y)
		));
	}
	
	public static String coordsToString(Point2D.Double point) {
		return coordsToString(Collections.singletonList(point));
	}
	
	public static String coordsToString(List<Point2D.Double> points) {		
		StringBuilder list = new StringBuilder();
		for( Point2D.Double point : points ) {		
			if( list.length() > 0 ) list.append(" ");
			list.append(point.x).append(",").append(point.y).append(",").append(0);
		}		
		return list.toString();
	}
	
	public static List<Point2D.Double> coordsFromString( String coordinateString ) {
		List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
		String[] coordinateArray = coordinateString.trim().split( " " );

		for( String coordinate : coordinateArray )
		{
			String[] value = coordinate.split( "," );
			if( value.length > 1 )
			{
				double x = Double.parseDouble( value[0].trim() );
				double y = Double.parseDouble( value[1].trim() );
				coords.add(new Point2D.Double(x, y));
			}
		}
		
		return coords;
	}
	
	public static List<Point2D.Double> coordsFromStringReverse( String coordinateString ) {
		List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
		String[] coordinateArray = coordinateString.trim().split( " " );

		for( String coordinate : coordinateArray )
		{
			String[] value = coordinate.split( "," );
			if( value.length > 1 )
			{
				double y = Double.parseDouble( value[0].trim() );
				double x = Double.parseDouble( value[1].trim() );
				coords.add(new Point2D.Double(x, y));
			}
		}
		
		return coords;
	}
	
	public static String toABGR( Color color ) {
		return getHexColor(color, true);
	}
	
	public static Color fromABGR( String abgr ) {
		return getHexColor(abgr, true);
	}
	
	public static String toARGB( Color color ) {
		return getHexColor(color, false);
	}
	
	public static Color fromARGB( String argb ) {
		return getHexColor(argb, false);
	}
	
	private static Color getHexColor(String colorCode, boolean bgr) {
		if( colorCode.startsWith("#") ) {
			colorCode = colorCode.substring(1);
		}
		
		long abgr = Long.parseLong( colorCode, 16 );
		int alpha = (int)((abgr & 0xff000000L) >> 24);
		int blue = (int)((abgr & 0x00ff0000L) >> 16);
		int green = (int)((abgr & 0x0000ff00L) >> 8);
		int red = (int)((abgr & 0x000000ffL));

		if( !bgr ) {
			int tmp = blue;
			blue = red;
			red = tmp;
		}

		return new Color(red, green, blue, alpha);
	}
	
	private static String getHexColor(Color color, boolean bgr) {

		int a = color.getAlpha();
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		if( !bgr ) {
			int tmp = b;
			b = r;
			r = tmp;
		}

		StringBuilder res = new StringBuilder();

		int offset = 0;
		res.append(Long.toHexString(a));
		if( res.length() - offset < 2 ) {
			res.insert(offset, '0');
		}
		offset = 2;
		res.append(Long.toHexString(b));
		if( res.length() - offset < 2 ) {
			res.insert(offset, '0');
		}
		offset = 4;
		res.append(Long.toHexString(g));
		if( res.length() - offset < 2 ) {
			res.insert(offset, '0');
		}
		offset = 6;
		res.append(Long.toHexString(r));
		if( res.length() - offset < 2 ) {
			res.insert(offset, '0');
		}
		return res.toString();
	}
	
	public static void writeExtendedData( String key, String value, XmlSerializer ser ) throws IOException {
		ser.startTag("", "Data").attribute("", "name", key).attribute("", "value", value).endTag("", "Data");
	}
	
	
	// extended data mappings
	
	public static void writeDrawingType( DrawingItem item, XmlSerializer ser ) throws IOException {
		String brushName = "None";
		// None, Freehand, Text, Number, Grid, Line, Arrow, Polyline, Rectangle, Circle, Polygon, GpxLine, GpxWayPoint, Other
		if( item instanceof FreeLine ) brushName = "Freehand";
		else if( item instanceof Number ) brushName = "Number";
		else if( item instanceof Arrow ) brushName = "Arrow";
		else if( item instanceof Polyline && ((Polyline) item).isClosed()) brushName = "Polygon";
		else if( item instanceof Polyline && !((Polyline) item).isClosed()) brushName = "Polyline";
		else if( item instanceof Ellipse ) brushName = "Circle";
		else if( item instanceof Rectangle ) brushName = "Rectangle";
		writeExtendedData(KEY_DATA_NAME_DRAWINGTYPE, brushName, ser);
	}
	
	public static void writeLineWidth( DrawingItem item, XmlSerializer ser ) throws IOException {
		DrawingStyle style = new DrawingStyle();
		item.updateStyle(style);
		Integer lineWidth = style.getLineWidth();
		if( lineWidth != null ) {
			String value = "Medium";
			if( lineWidth > 4 ) {
				value = "Large";	
			} else if( lineWidth < 4 ) {
				value = "Small";
			}
			writeExtendedData(KEY_DATA_NAME_WIDTH, value, ser);
		}
	}
	
	public static void writeLineStyle( DrawingItem item, XmlSerializer ser ) throws IOException {
		DrawingStyle style = new DrawingStyle();
		item.updateStyle(style);
		PathEffect pathEffect = style.getPathEffect();		
		if( pathEffect != null ) {
			String value = "Solid";
			if( pathEffect != PathEffect.None ) {
				value = "Dashed";	
			}
			writeExtendedData(KEY_DATA_NAME_LINE_STYLE, value, ser);
		}
	}
	
	public static void writeFontStyle( DrawingItem item, XmlSerializer ser ) throws IOException {
		DrawingStyle style = new DrawingStyle();
		item.updateStyle(style);
		Integer fontStyle = style.getFontStyle();		
		if( fontStyle != null ) {
			String value = "Normal";
			if( fontStyle == Font.BOLD ) {
				value = "Bold";	
			}
			writeExtendedData(KEY_DATA_NAME_LINE_STYLE, value, ser);
		}
	}
	
	public static void writeBackgroundColor( DrawingItem item, XmlSerializer ser ) throws IOException {
		DrawingStyle style = new DrawingStyle();
		item.updateStyle(style);
		Color background = style.getBackground();		
		if( background != null ) {			
			writeExtendedData(KEY_DATA_NAME_BACKGROUND, toABGR(background), ser);
		}
	}
	
	private KmlDrawUtils() {} 
}
