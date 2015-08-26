package tim.prune.load.xml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.FileInfo;
import tim.prune.draw.Rotatable;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.PathEffect;
import tim.prune.drawing.item.Arrow;
import tim.prune.drawing.item.Ellipse;
import tim.prune.drawing.item.Polyline;
import tim.prune.drawing.item.Rectangle;
import tim.prune.drawing.item.Text;
import tim.prune.drawing.kml.KmlDrawUtils;
import tim.prune.save.FontStyle;
import tim.prune.util.EllipsoidHelper;

public class KmlDrawingHandler extends XmlHandler {

	private enum StyleType {
		None, Line, Poly, Axis, Arc, Point, Label
	}

	private StyleType styleType;
	private Drawing drawing;
	private DrawingStyle style;
	private Class<? extends DrawingItem> type;
	private String placemarkName;
	private StringBuilder characters;
	private Float semiMajorAxis;
	private Float semiMinorAxis;
	private Color lineStyle;
	private Color labelStyle;
	private String angle;
	private String bounds;
	private List<Point2D.Double> coords;
	private boolean closed;
	
	public KmlDrawingHandler(Drawing drawing) {
		this.drawing = drawing;
		characters = new StringBuilder();
		styleType = StyleType.None;

		reset();
	}


	/**
	 * Resets the DrawingParser to use data from specified MediaContent.
	 * @param media The MediaContent to be parsed.
	 */
	public void reset()
	{
		style = new DrawingStyle();
		styleType = StyleType.None;
		characters.delete(0, characters.length());
		placemarkName = null;
		angle = null;
		bounds = null;
//		rotPoint = null;
		type = null;
		closed = false;
		coords = null;
	}

	@Override
	public void characters( char[] buffer, int start, int length )
	{
		characters.append( buffer, start, length );
	}

	@Override
	public void startElement(String uri, String name, String qName,
			Attributes attrs) throws SAXException {
		if( "".equals(name) ) {
			name = qName;
		}

		startPlacemark(name);
		startItem(name, attrs);
		startStyle(name);
		startItem(name);
		startTextBox(name, attrs);
		startExtDataData(name, attrs);
	}


	@Override
	public void endElement(String uri, String name, String qName)
	throws SAXException {

		if( "".equals(name) ) {
			name = qName;
		}

		endColor(name);
		endLineStyles(name);
		endFontSize(name);
		endFontStyle(name);
		endAngle(name);
		endWidth(name);
		endCoordinates(name);
		endName(name);
		endSemiMajorAxis(name);
		endSemiMinorAxis(name);
		endCircle(name);
		endPlacemark(name);

		characters.delete(0, characters.length());
	}

	private void startPlacemark(String name) {
		if( name.equals( KmlDrawUtils.ELEM_PLACEMARK ) )
		{
			reset();
		}
	}

	@SuppressWarnings("unchecked")
	private void startItem(String name, Attributes attrs) {
		if( name.equals(KmlDrawUtils.ELEM_DRAWING_ITEM) )
		{
			String value = attrs.getValue("", "type");
			if( value != null ) {
				try {
					type = (Class<? extends DrawingItem>) Class.forName(value);
				} catch (ClassNotFoundException e) { 
					// ignored, we'll create it elsewhere
				}
			}
		}
	}

	private void startTextBox(String name, Attributes attrs) {
		if( name.equals("TextBox") )
		{
			String colorCode = attrs.getValue("", "fgColor");
			if( !isEmpty(colorCode) ) {
				style.setForeground(KmlDrawUtils.fromARGB(colorCode));
			}

			colorCode = attrs.getValue("", "bgColor");
			if( !isEmpty(colorCode) ) {
				style.setBackground(KmlDrawUtils.fromARGB(colorCode));
			}

			String angle = attrs.getValue("", "angle");
			if( !isEmpty(angle) ) {
				this.angle = angle;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void startExtDataData( String name, Attributes attrs ) {
		if( name.equals(KmlDrawUtils.ELEM_DATA) ) {
			String extDataKey = attrs.getValue("", KmlDrawUtils.ATTR_DATA_NAME);
			String extDataVal = attrs.getValue("", KmlDrawUtils.ATTR_DATA_VALUE);

			if( extDataKey.equals(KmlDrawUtils.KEY_DATA_NAME_TYPE) ) {
				try {
					type = (Class<? extends DrawingItem>) Class.forName(extDataVal);
				} catch (ClassNotFoundException e) { 
					// ignored, we'll create it elsewhere
				}
			} else if( extDataKey.equals(KmlDrawUtils.KEY_DATA_NAME_ANGLE) ) {
				angle = extDataVal;

			} else if( extDataKey.equals(KmlDrawUtils.KEY_DATA_NAME_BOUNDS) ) {
				bounds = extDataVal;

			} else if( extDataKey.equals(KmlDrawUtils.KEY_DATA_NAME_ROT_POINT) ) {
				// Unused right now
				// rotPoint = characters.toString();
			}			
		}
	}


	private void startItem(String name) {
		if( type == null ) {
			if( name.equals( KmlDrawUtils.ELEM_LINE_STRING ) )
			{
				type = Polyline.class;
			}
			else if( name.equals( KmlDrawUtils.ELEM_RECTANGLE ) || name.equals( KmlDrawUtils.ELEM_SQUARE ) )
			{
				type = Rectangle.class;
			}
			else if( name.equals( KmlDrawUtils.ELEM_POLYGON ) )
			{
				type = Polyline.class;
				closed = true;
			}
			else if( name.equals( KmlDrawUtils.ELEM_POINT ) )
			{
				type = Text.class;
			}
			else if( name.equals( KmlDrawUtils.ELEM_CIRCLE ) )
			{
				type = Ellipse.class;
			}
			else if( name.equals(KmlDrawUtils.ELEM_ARROW) )
			{
				type = Arrow.class;
			}
		}
	}


	private void startStyle(String name) {
		if( name.equals( KmlDrawUtils.ELEM_LINE_STYLE ) )
		{
			styleType = StyleType.Line;
		}
		else if( name.equals( KmlDrawUtils.ELEM_POLY_STYLE ) || name.equals( KmlDrawUtils.ELEM_AXIS_STYLE ) )
		{
			styleType = StyleType.Poly;
		}
		else if( name.equals( KmlDrawUtils.ELEM_ARC_STYLE ) )
		{
			styleType = StyleType.Arc;
		}
		else if( name.equals( KmlDrawUtils.ELEM_POINT_STYLE ) )
		{
			styleType = StyleType.Point;
		}
		else if( name.equals( KmlDrawUtils.ELEM_LABEL_STYLE ) )
		{
			styleType = StyleType.Label;
		}
	}
	
	private boolean isEmpty(String colorCode) {
		return colorCode == null || colorCode.trim().length() == 0;
	}


	private void endPlacemark(String name) {
		if( name.equals(KmlDrawUtils.ELEM_PLACEMARK) ) 
		{
			
			// now build
			DrawingItem item = drawing.addItem(type);
			item.setPoints(coords);
			
			if( item instanceof Polyline ) {
				((Polyline) item).setClosed(closed);
			}
			
			if( item instanceof Text ) {
				((Text) item).setText(placemarkName);

				// I don't know why this is true, but for some reason maps uses
				// line style as the background for labels...only labels! so dumb
				style.setForeground(labelStyle);
				style.setBackground(lineStyle);
			} else {
				style.setForeground(lineStyle);
			}
			
			if( item instanceof Rectangle && bounds != null ) {
				List<Double> coords = KmlDrawUtils.coordsFromString(bounds);
				if( coords.size() > 2 ) {
					Double sw = coords.get(0);
					Double ne = coords.get(2);
					item.setPoints(Arrays.asList(sw, ne));
				}
			}
			
			if( item instanceof Rotatable && angle != null ) {
				try {
					((Rotatable) item).rotate(java.lang.Double.parseDouble(angle), null);
				} catch ( Exception ignored ) {
					ignored.printStackTrace();
				}
			}

			item.applyStyle(style);
		}
	}


	private void endName(String name) {
		if( name.equals(KmlDrawUtils.ELEM_PLACEMARK_NAME) )
		{
			placemarkName = characters.toString().trim();
		}
	}


	private void endCoordinates(String name) {
		if( name.equals( KmlDrawUtils.ELEM_COORDINATES ) )
		{
			coords = KmlDrawUtils.coordsFromString(characters.toString());
		}
	}


	private void endWidth(String name) {
		if( name.equals( KmlDrawUtils.ELEM_WIDTH ) )
		{
			style.setLineWidth((int) Float.parseFloat( characters.toString().trim() ));
		}
	}


	private void endFontSize(String name) {
		if( name.equals( KmlDrawUtils.ELEM_FONT_SIZE ) || name.equals( KmlDrawUtils.ELEM_TEXT_SIZE ) )
		{
			String fontSize = characters.toString().trim();
			style.setFontSize((int)Float.parseFloat( fontSize ));
		}
		else if( name.equals( KmlDrawUtils.ELEM_SCALE ) )
		{
			String data = characters.toString().trim();
			float scale = Float.parseFloat(data);
			if( styleType == StyleType.Point ) {
				style.setFontSize((int) (DrawingStyle.DEFAULT_FONT_SIZE*scale));
			}
		}
	}
	
	private void endFontStyle(String name) {
		if( name.equals( KmlDrawUtils.ELEM_FONT_STYLE ) )
		{
			String fontStyle = characters.toString().trim();
			FontStyle valueOf = FontStyle.fromString(fontStyle);
			if( valueOf != null ) {
				style.setFontStyle(valueOf.toJavaFontStyle());
			}
		}
	}
	
	private void endAngle(String name) {
		if( name.equals( KmlDrawUtils.ELEM_ANGLE ) )
		{
			angle = characters.toString().trim();
		}
	}
	
	private void endLineStyles(String name) {
		if( name.equals( KmlDrawUtils.ELEM_LINE_STYLES ) )
		{
			String encoding = characters.toString().trim();

			if( encoding.contains( "/DR" ) )
			{
				try
				{
					String[] strings = encoding.split( "," );

					if( strings.length < 3 )
					{
						style.setPathEffect(PathEffect.Dash);
					}
					else
					{
						style.setPathEffect(PathEffect.DashDot);
					}
				}
				catch( Exception e )
				{
					style.setPathEffect(PathEffect.Dash);
				}
			}
		}
	}


	private void endColor(String name) {
		if( name.equals( KmlDrawUtils.ELEM_COLOR ) )
		{
			String colorCode = characters.toString().trim();
			if( !isEmpty( colorCode ) ) {
				if( styleType == StyleType.Poly) {
					style.setBackground(KmlDrawUtils.fromABGR(colorCode));
					
				} else if (styleType == StyleType.Arc) {
					style.setBackground(KmlDrawUtils.fromABGR(colorCode));
					
				} else if( styleType == StyleType.Line ) {
					lineStyle = KmlDrawUtils.fromABGR(colorCode);
					
				} else if (styleType == StyleType.Label) {
					labelStyle = KmlDrawUtils.fromABGR(colorCode);
				}
			}
		}
		else if( name.equals( KmlDrawUtils.ELEM_BG_COLOR ) )
		{
			String colorCode = characters.toString().trim();
			if( !isEmpty(colorCode) ){
				style.setBackground(KmlDrawUtils.fromABGR(colorCode));
			}
		}
	}

	private void endSemiMajorAxis(String name) {
		
		if (name.equals(KmlDrawUtils.ELEM_SEMI_MAJOR_AXIS)) {
			
			String axis = characters.toString().trim();
			semiMajorAxis = Float.parseFloat( axis );
		}
	}
	
	private void endSemiMinorAxis(String name) {
		
		if (name.equals(KmlDrawUtils.ELEM_SEMI_MINOR_AXIS)) {
			
			String axis = characters.toString().trim();
			semiMinorAxis = Float.parseFloat(axis);
		}
	}
	
	private void endCircle(String name) {
	
		if (name.equals(KmlDrawUtils.ELEM_CIRCLE) && coords != null && coords.size() == 1 ) {
			// This is where we convert from center of circle + semi major axes to 
			// 2 corner points

			Point2D.Double circleCenter = (Point2D.Double) coords.get(0);

			// Remove the circle center point from the list so we don't have an extra point
			coords.remove(0);

			double semiMajorAxisInDegrees = EllipsoidHelper.metersToDegrees(semiMajorAxis);
			double semiMinorAxisInDegrees = EllipsoidHelper.metersToDegrees(semiMinorAxis);
			
			Point2D.Double nwPoint = new Point2D.Double(circleCenter.x - semiMajorAxisInDegrees,
					circleCenter.y - semiMinorAxisInDegrees);
			coords.add(nwPoint);

			Point2D.Double sePoint = new Point2D.Double(circleCenter.x + semiMajorAxisInDegrees,
					circleCenter.y + semiMinorAxisInDegrees);

			coords.add(sePoint);
		}
	}
	
	@Override
	public FileInfo getFileInfo() {
		return new FileInfo();
	}
}
