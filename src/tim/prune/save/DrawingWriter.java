package tim.prune.save;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import tim.prune.draw.Rotatable;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.ItemFactory;
import tim.prune.drawing.PathEffect;
import tim.prune.drawing.item.Arrow;
import tim.prune.drawing.item.Ellipse;
import tim.prune.drawing.item.Group;
import tim.prune.drawing.item.Rectangle;
import tim.prune.drawing.item.Text;
import tim.prune.drawing.kml.KmlDrawUtils;

public class DrawingWriter {

	private static final String PROP_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
	
	private final Drawing drawing;

	public DrawingWriter(Drawing drawing) {
		this.drawing = drawing;
	}

	public void write( File file ) throws Exception {
		XmlSerializer serializer = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
			xppf.setNamespaceAware(false);
			serializer = xppf.newSerializer();
			serializer.setFeature(PROP_INDENT, true);
			serializer.setOutput(fos, "utf8");
			new KmlWriter(serializer).writeItems(drawing.getItems());
		} catch ( Exception e ) {
			e.printStackTrace();
			throw e;
		} finally {
			if( serializer != null ) serializer.flush();
			if( fos != null ) fos.close();
		}
	}


	public final class KmlWriter {

		private final XmlSerializer serializer;

		public KmlWriter( XmlSerializer serializer ) {
			this.serializer = serializer;
		}		

		public XmlSerializer getSerializer() {
			return serializer;
		}

		public void writeItems( List<DrawingItem> items ) throws IOException {
			serializer.startDocument(null, null);
			serializer.startTag("", "kml");
			serializer.startTag("", "Document");

			for( DrawingItem item : items ) {
				if(!(item instanceof Group)) { // shouldn't happen but just incase
					writeItem(item);
				}
			}

			serializer.endTag("", "Document");
			serializer.endTag("", "kml");
			serializer.endDocument();
		}

		public void writeItem( DrawingItem item ) throws IOException {
			serializer.startTag("", "Placemark");
			if( item instanceof Text && ((Text)item).getText() != null ) {
				writeName(((Text)item).getText());
			}
			writeStyle(item);
			writeGeometry(item);
			writeMetadata(item);
			serializer.endTag("", "Placemark");
		}

		public void writeStyle( DrawingItem item ) throws IOException {
			serializer.startTag("", "Style");
			if( item instanceof Text ) {
				writePointStyle(item);
			} else if ( item instanceof Ellipse ) {				
				writeLineStyle(item);
				writeArcStyle(item);
			} else {
				writeLineStyle(item);
				writePolyStyle(item);				
			}
			serializer.endTag("", "Style");
		}

		private void writePolyStyle(DrawingItem item) throws IOException {
			DrawingStyle style = new DrawingStyle();
			item.updateStyle(style);
			serializer.startTag("", "PolyStyle");
			writeColor(KmlDrawUtils.toABGR(style.getBackground()));
			serializer.endTag("", "PolyStyle");
		}

		private void writeArcStyle(DrawingItem item) throws IOException {

			DrawingStyle style = new DrawingStyle();
			item.updateStyle(style);

			serializer.startTag("", "ArcStyle");
			writeColor(KmlDrawUtils.toABGR(style.getBackground()));

			serializer.endTag("", "ArcStyle");
		}

		private void writeMetadata(DrawingItem item) throws IOException {
			serializer.startTag("", "ExtendedData");
			
			// type extended data
			KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_TYPE, ItemFactory.getClassName(item), serializer);
			
			// bounds extended data
			Rectangle2D.Double bounds = item.getBounds();
			KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_BOUNDS, KmlDrawUtils.coordsToString(bounds), serializer);
			
			// rotatable stuff
			if( item instanceof Rotatable ) {
				Rotatable rotatable = ((Rotatable) item);
				if( rotatable.getRotationAngle() != 0 && rotatable.getRotationPoint() != null ) {
					KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_ANGLE, String.valueOf((int)rotatable.getRotationAngle()), serializer);
					KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_ROT_POINT, KmlDrawUtils.coordsToString(rotatable.getRotationPoint()), serializer);
				}
			}
			
			// version c type extended data
			KmlDrawUtils.writeDrawingType(item, serializer);
			KmlDrawUtils.writeFontStyle(item, serializer);
			KmlDrawUtils.writeLineStyle(item, serializer);
			KmlDrawUtils.writeLineWidth(item, serializer);
			KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_TOPLEFT, KmlDrawUtils.coordsToStringReverse(new Point2D.Double(bounds.x,bounds.y+bounds.height)), serializer);
			KmlDrawUtils.writeExtendedData(KmlDrawUtils.KEY_DATA_NAME_BOTTOMRIGHT, KmlDrawUtils.coordsToStringReverse(new Point2D.Double(bounds.x+bounds.width,bounds.y)), serializer);
			
			serializer.endTag("", "ExtendedData");
		}

		private void writeLineStyle(DrawingItem item) throws IOException {
			DrawingStyle style = new DrawingStyle();
			item.updateStyle(style);
			serializer.startTag("", "LineStyle");
			writeColor(KmlDrawUtils.toABGR(item.getForeground()));
			writeWidth(style.getLineWidth());
			PathEffect pathEffect = style.getPathEffect();
			if( pathEffect != null && pathEffect != PathEffect.None ) {
				float[] pattern = pathEffect.getPattern();
				StringBuilder patternStr = new StringBuilder();
				for( float f : pattern ) {
					if( patternStr.length() > 0 ) patternStr.append(",");
					patternStr.append(f);
				}
				serializer.startTag("", "lineStyles");
				serializer.text("/DR="+patternStr);
				serializer.endTag("", "lineStyles");
			}
			serializer.endTag("", "LineStyle");

		}

		private void writePointStyle(DrawingItem item) throws IOException{
			DrawingStyle style = new DrawingStyle();
			item.updateStyle(style);

			serializer.startTag("", "LabelStyle");
			writeColor(KmlDrawUtils.toABGR(style.getForeground()));
			serializer.endTag("", "LabelStyle");

			// I don't know why this is true, but for some reason maps uses
			// line style as the background for labels...only labels! so dumb
			serializer.startTag("", "LineStyle");
			writeColor(KmlDrawUtils.toABGR(item.getBackground()));
			serializer.endTag("", "LineStyle");

			serializer.startTag("", "fontWeight");
			if( (style.getFontStyle() & Font.BOLD) > 0 ) {
				serializer.text("bold");
			} else {
				serializer.text("normal");				
			}
			serializer.endTag("", "fontWeight");

			serializer.startTag("", "fontSizeUnits");
			serializer.text("pt");
			serializer.endTag("", "fontSizeUnits");

			serializer.startTag("", "fontSize");
			serializer.text(String.valueOf(style.getFontSize()));
			serializer.endTag("", "fontSize");

			serializer.startTag("", "fontStyle");
			serializer.text(FontStyle.toFontStyle(style.getFontStyle()).toString());
			serializer.endTag("", "fontStyle");

			serializer.startTag("", "angle");
			serializer.text(String.valueOf((int)((Rotatable)item).getRotationAngle()));
			serializer.endTag("", "angle");

			writeScale((((float)style.getFontSize())/(((float)DrawingStyle.DEFAULT_FONT_SIZE))));
		}

		public void writeScale(float scale) throws IOException {
			serializer.startTag("", "scale");
			serializer.text("" + scale);
			serializer.endTag("", "scale");
		}

		public void writeWidth(int size) throws IOException {
			serializer.startTag("", "width");
			serializer.text("" + size);
			serializer.endTag("", "width");
		}

		public void writeColor(String color) throws IOException {
			serializer.startTag("", "color");
			serializer.text(color);
			serializer.endTag("", "color");
		}

		public void writeFill(String fill) throws IOException {
			serializer.startTag("", "fill");
			serializer.text(fill);
			serializer.endTag("", "fill");
		}

		public void writeFill(int fill) throws IOException {
			writeFill(String.valueOf(fill));
		}

		public void writeGeometry( DrawingItem item ) throws IOException {
			if( item instanceof Ellipse ) {
				serializer.startTag("", "Circle");

				Double center = getCircleCenter(item.getPoints());

				List<Double> points = new ArrayList<Double>();
				points.add(center);
				writePoints(points);
				serializer.endTag("", "Circle");
			} else if( item instanceof Rectangle ) {
				serializer.startTag("", "Rectangle");
				serializer.startTag("", "outerBoundaryIs");
				serializer.startTag("", "LinearRing");
				serializer.startTag("", "tessellate");
				serializer.text("1");
				serializer.endTag("", "tessellate");
				writePoints(item.getPoints());
				serializer.endTag("", "LinearRing");
				serializer.endTag("", "outerBoundaryIs");
				serializer.endTag("", "Rectangle");				
			} else if( item instanceof Text || item instanceof Number ) {
				serializer.startTag("", "Point");
				writePoints(item.getPoints());
				serializer.endTag("", "Point");
			} else if( item instanceof Arrow ) {
				serializer.startTag("", KmlDrawUtils.ELEM_ARROW);
				writePoints(item.getPoints());
				serializer.endTag("", KmlDrawUtils.ELEM_ARROW);				
			} else {
				serializer.startTag("", "LineString");
				writePoints(item.getPoints());
				serializer.endTag("", "LineString");				
			}
		}

		public void writeName(String name) throws IOException{
			serializer.startTag("", "name");
			serializer.text(name);
			serializer.endTag("", "name");
		}

		public void writePoints( List<Double> points ) throws IOException {
			serializer.startTag("", "coordinates");
			serializer.text(KmlDrawUtils.coordsToString(points));
			serializer.endTag("", "coordinates");
		}

		private Double getCircleCenter(List<Double> points) {

			Double point1 = points.get(0);
			Double point2 = points.get(1);

			Double center = new Double();
			center.x = (point1.x + point2.x)/2;
			center.y = (point1.y + point2.y)/2;

			return center;
		}
	}
}
