package tim.prune.gui.map.mgrs;

/**
 * Code derived from http://dhost.info/usngweb/ codebase.
 * Applies only to package tim.prune.gui.map.mgrs
 * Original copyright:
 * 
 * Copyright (c) 2009 Larry Moore, larmoor@gmail.com
 * Released under the MIT License 
 * http://www.opensource.org/licenses/mit-license.php 
 * http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import tim.prune.App;
import tim.prune.gui.Viewport;
import tim.prune.gui.map.MapCanvas;
import tim.prune.overlay.Overlay;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

public class MGRSOverlay extends Overlay {
	private static final float MGRS_ALPHA = .4f;
	private static final int MGRS_LINE_WIDTH = 2;
	private static final Color MGRS_POLY_COLOR = new Color(0,1f,0,MGRS_ALPHA);
	private static final Color MGRS_LINE_COLOR = new Color(.2f,.2f,.2f,MGRS_ALPHA);
	private static MGRSOverlay instance;

	private void drawString(Graphics2D graphics, String text, int x, int y) {
		Font tf = graphics.getFont();
		Color tc = graphics.getColor();
		graphics.setFont(new Font("Monospaced", Font.BOLD, 12));
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x-1, y);
		graphics.drawString(text, x+1, y);
		graphics.drawString(text, x, y-1);
		graphics.drawString(text, x, y+1);
		graphics.setColor(Color.WHITE);
		graphics.drawString(text, x, y);
		graphics.setFont(tf);
		graphics.setColor(tc);
	}

	private List<Integer> xPolyCords;
	private List<Integer> yPolyCords;
	private void startPolyLine() {
		xPolyCords = new ArrayList<Integer>();
		yPolyCords = new ArrayList<Integer>();
	}

	private void finishPolyLine(Graphics2D graphics, int width) {
		int points = xPolyCords.size();
		int[] x = new int[points];
		int[] y = new int[points];
		int c = 0;
		for (int i : xPolyCords) {
			x[c++] = i;
		}
		c = 0;
		for (int i : yPolyCords) {
			y[c++] = i;
		}
		xPolyCords = null;
		yPolyCords = null;
		Stroke ts = graphics.getStroke();
		Color tc = graphics.getColor();
		graphics.setColor(MGRS_POLY_COLOR);
		graphics.setStroke(new BasicStroke(width));
		graphics.drawPolyline(x, y, points);
		graphics.setStroke(ts);
		graphics.setColor(tc);
	}

	private void addPolyLine(int x1, int y1, int x2, int y2) {
		if (xPolyCords.size() == 0) {
			xPolyCords.add(x1);
			yPolyCords.add(y1);
		}
		xPolyCords.add(x2);
		yPolyCords.add(y2);
	}

	private void drawLine(Graphics2D graphics, int x1, int y1, int x2, int y2,
			int width) {
		Stroke ts = graphics.getStroke();
		Color tc = graphics.getColor();
		graphics.setColor(MGRS_LINE_COLOR);
		graphics.setStroke(new BasicStroke(width));
		graphics.drawLine(x1, y1, x2, y2);
		graphics.setStroke(ts);
		graphics.setColor(tc);
	}

	private void renderMGRSGrid(MapCanvas canvas, Graphics2D graphics,
			MGRSViewport lines) {
		int height = canvas.getHeight();
		int width = canvas.getWidth();
		for (Double f : lines.getLats()) {
			int y = canvas.getMapPosition().getYFromLat(f, height);
			drawLine(graphics, 0, y, width, y, MGRS_LINE_WIDTH);
		}
		for (Double f : lines.getLngs()) {
			int x = canvas.getMapPosition().getXFromLong(f, width);
			drawLine(graphics, x, 0, x, height, MGRS_LINE_WIDTH);
		}
	}

	private int outcode(GeoRectangle rec, double lat, double lng) {
		int code = 0;
		if (lat < rec.getSW().getLatitude()) {
			code |= 4;
		}
		if (lat > rec.getNE().getLatitude()) {
			code |= 8;
		}
		if (lng < rec.getSW().getLongitude()) {
			code |= 1;
		}
		if (lng > rec.getNE().getLongitude()) {
			code |= 2;
		}
		return code;
	}

	private boolean inside(GeoRectangle rec, double lat, double lng) {
		if (lat < rec.getSW().getLatitude() || lat > rec.getNE().getLatitude()) {
			return false;
		}
		if (lng < rec.getSW().getLongitude()
				|| lng > rec.getNE().getLongitude()) {
			return false;
		}
		return true;
	}

	/**
	 * Implementation of Cohen-Sutherland clipping algorithm to clip grid lines
	 * at boundarie of utm zones and the viewport edges
	 *
	 * @param canvas
	 * @param rec
	 * @param llp1
	 * @param llp2
	 * @return Array of clipped points or null if both are outside rec.
	 */
	private LatLonPoint[] checkClip(MapCanvas canvas, GeoRectangle rec, LatLonPoint llp1,
			LatLonPoint llp2) {
		boolean swapped = false;
		double u1 = llp1.getLongitude();
		double v1 = llp1.getLatitude();
		double u2 = llp2.getLongitude();
		double v2 = llp2.getLatitude();
		LatLonPoint ret = null;
		int code1 = outcode(rec, v1, u1);
		int code2 = outcode(rec, v2, u2);
		if ((code1 & code2) != 0) { // line segment outside window...don't draw it
			return null;
		}
		if ((code1 | code2) == 0) { // line segment completely inside window...draw it
			return new LatLonPoint[] {llp1, llp2};
		}
		if (inside(rec, v1, u1)) { // coordinates must be altered
			// swap coordinates
			swapped = true;
			double temp = u1;
			u1 = u2;
			u2 = temp;
			temp = v1;
			v1 = v2;
			v2 = temp;
			int ctemp = code1;
			code1 = code2;
			code2 = ctemp;
		}
		if ((code1 & 8) != 0) { // clip along northern edge of polygon
			double t = (rec.getNE().getLatitude() - v1) / (v2 - v1);
			u1 += t * (u2 - u1);
			v1 = rec.getNE().getLatitude();
			ret = new LatLonPoint(v1, u1);
		} else if ((code1 & 4) != 0) { // clip along southern edge
			double t = (rec.getSW().getLatitude() - v1) / (v2 - v1);
			u1 += t * (u2 - u1);
			v1 = rec.getSW().getLatitude();
			ret = new LatLonPoint(v1, u1);
		} else if ((code1 & 1) != 0) { // clip along west edge
			double t = (rec.getSW().getLongitude() - u1) / (u2 - u1);
			v1 += t * (v2 - v1);
			u1 = rec.getSW().getLongitude();
			ret = new LatLonPoint(v1, u1);
		} else if ((code1 & 2) != 0) { // clip along east edge
			double t = (rec.getNE().getLongitude() - u1) / (u2 - u1);
			v1 += t * (v2 - v1);
			u1 = rec.getNE().getLongitude();
			ret = new LatLonPoint(v1, u1);
		}
		return swapped?new LatLonPoint[] {llp1, ret}:new LatLonPoint[] {ret, llp2};
	}

	private void render100kLabels(MapCanvas canvas, Graphics2D graphics,
			List<Float> eastings, List<Float> northings) {
		if (canvas.getMapPosition().getZoom() > 15)
			return;
		int height = canvas.getHeight();
		int width = canvas.getWidth();
		for (int i = 0; i < eastings.size() - 1; i++) {
			for (int j = 0; j < northings.size() - 1; j++) {
				float e = (eastings.get(i) + eastings.get(i + 1)) / 2;
				float n = (northings.get(j) + northings.get(j + 1)) / 2;
				LatLonPoint p = new LatLonPoint(n, e);
				MGRSPoint mp = new MGRSPoint(p);
				String label = "";
				if (canvas.getMapPosition().getZoom() < 10) {
					label = mp.get100kID();
				} else {
					label = mp.getZone() + " " + mp.get100kID();
				}
				int y = canvas.getMapPosition().getYFromLat(p.getLatitude(),
						height);
				int x = canvas.getMapPosition().getXFromLong(p.getLongitude(),
						width);
				FontMetrics metrics = graphics.getFontMetrics();
				drawString(graphics, label,
						x - (metrics.stringWidth(label) / 2), y);
			}
		}
	}

	private void renderLabels(MapCanvas canvas, Graphics2D graphics,
			List<Float> eastings, List<Float> northings, int digits) {
		float n;
		float e;
		int height = canvas.getHeight();
		int width = canvas.getWidth();
		// Eastings along bottom.
		n = (northings.get(0) + northings.get(1)) / 2;
		for (int i = 1; i < eastings.size() - 1; i++) {
			e = (eastings.get(i) + eastings.get(i + 1)) / 2;
			LatLonPoint p = new LatLonPoint(n, e);
			MGRSPoint mp = new MGRSPoint(p);
			String label = "";
			if (mp.zone_number > 9) {
				label = mp.getMGRS().substring(5,5+digits);
			} else {
				label = mp.getMGRS().substring(4,4+digits);
			}
			int y = height - 3;
			int x = canvas.getMapPosition().getXFromLong(eastings.get(i),
					width);
			drawString(graphics, label, x+ 3, y);
		}
		// Northings along side.
		e = (eastings.get(0) + eastings.get(1)) / 2;
		for (int j = 1; j < northings.size() - 1; j++) {
			n = (northings.get(j) + northings.get(j + 1)) / 2;
			LatLonPoint p = new LatLonPoint(n, e);
			MGRSPoint mp = new MGRSPoint(p);
			String label = "";
			if (mp.zone_number > 9) {
				label = mp.getMGRS().substring(10,10+digits);
			} else {
				label = mp.getMGRS().substring(9,9+digits);
			}
			int y = canvas.getMapPosition().getYFromLat(northings.get(j),
					height);
			int x = 3;
			drawString(graphics, label, x, y - 3);
		}
	}

	private void connectPoints(MapCanvas canvas,
			GeoRectangle rec, int height, int width,
			LatLonPoint llp1, LatLonPoint llp2) {
		LatLonPoint[] llps = checkClip(canvas, rec, llp1, llp2);
		if (llps != null) {
			int y1 = canvas.getMapPosition().getYFromLat(
					llps[0].getLatitude(), height);
			int x1 = canvas.getMapPosition().getXFromLong(
					llps[0].getLongitude(), width);
			int y2 = canvas.getMapPosition().getYFromLat(
					llps[1].getLatitude(), height);
			int x2 = canvas.getMapPosition().getXFromLong(
					llps[1].getLongitude(), width);
			addPolyLine(x1, y1, x2, y2);
		}
	}

	private void renderGridCell(MapCanvas canvas, Graphics2D graphics,
			GeoRectangle rec, int interval) {
		int height = canvas.getHeight();
		int width = canvas.getWidth();
		MGRSPoint sw = MGRSPoint.LLtoMGRS(rec.getSW(),
				rec.getMGRSCenter().zone_number);
		double sw_utm_e = (Math.floor(sw.easting / interval) * interval)
				- interval;
		double sw_utm_n = (Math.floor(sw.northing / interval) * interval)
				- interval;

		MGRSPoint ne = MGRSPoint.LLtoMGRS(rec.getNE(),
				rec.getMGRSCenter().zone_number);
		double ne_utm_e = (Math.floor(ne.easting / interval + 1) * interval)
				+ interval;
		double ne_utm_n = (Math.floor(ne.northing / interval + 1) * interval)
				+ interval;

		// set density of points on grid lines
		double precision = 1000; // In between, zoom levels 12-15
		if (canvas.getZoom() < 12) { // zoomed out a long way; not very dense
			precision = 10000;
		} else if (canvas.getZoom() > 19) { // Zoomed in a long LONG way
			precision = 10;
		} else if (canvas.getZoom() > 15) { // Zoomed in a long way
			precision = 100;
		}

		List<Float> northings = new ArrayList<Float>();
		northings.add(rec.getSW().getLatitude());
		for (double i = sw_utm_n; i < ne_utm_n; i += interval) {
			// collect coords to be used to place markers
			// '2*this.interval' is a fudge factor that approximately offsets
			// grid line convergence
			MGRSPoint gc = new MGRSPoint((float) i, (float) sw_utm_e
					+ (2 * interval), rec.getMGRSCenter().zone_number,
					rec.getMGRSCenter().zone_letter);
			LatLonPoint p = gc.toLatLonPoint();
			if ((p.getLatitude() > rec.getSW().getLatitude())
					&& (p.getLatitude() < rec.getNE().getLatitude())) {
				northings.add(p.getLatitude());
			}
			// calculate line segments of one e-w line
			LatLonPoint prev = null;
			startPolyLine();
			for (double m = sw_utm_e; m <= ne_utm_e; m += precision) {
				LatLonPoint t = (new MGRSPoint((float) i, (float) m,
						rec.getMGRSCenter().zone_number,
						rec.getMGRSCenter().zone_letter)).toLatLonPoint();
				if (prev != null) {
					connectPoints(canvas, rec, height, width, prev, t);
				}
				prev = t;
			}
			finishPolyLine(graphics, MGRS_LINE_WIDTH);
		}
		northings.add(rec.getNE().getLatitude());

		List<Float> eastings = new ArrayList<Float>();
		eastings.add(rec.getSW().getLongitude());
		// for each n-s line that covers the cell, with overedge
		for (double i = sw_utm_e; i < ne_utm_e; i += interval) {
			// collect coords to be used to place markers
			// '2*this.interval' is a fudge factor that approximately offsets
			// grid line convergence
			MGRSPoint gc = new MGRSPoint((float) sw_utm_n + (2 * interval),
					(float) i, rec.getMGRSCenter().zone_number,
					rec.getMGRSCenter().zone_letter);
			LatLonPoint p = gc.toLatLonPoint();
			if (p.getLongitude() > rec.getSW().getLongitude()
					&& p.getLongitude() < rec.getNE().getLongitude()) {
				eastings.add(p.getLongitude());
			}

			LatLonPoint prev = null;
			startPolyLine();
			for (double m = sw_utm_n; m <= ne_utm_n; m += precision) {
				LatLonPoint t = (new MGRSPoint((float) m, (float) i,
						rec.getMGRSCenter().zone_number,
						rec.getMGRSCenter().zone_letter)).toLatLonPoint();
				if (prev != null) {
					connectPoints(canvas, rec, height, width, prev, t);
				}
				prev = t;
			}
			finishPolyLine(graphics, MGRS_LINE_WIDTH);
		}
		eastings.add(rec.getNE().getLongitude());

		if (interval == 100000) {
			render100kLabels(canvas, graphics, eastings, northings);
		} else if (interval == 10000) {
			renderLabels(canvas, graphics, eastings, northings, 1);
		} else if (interval == 1000) {
			renderLabels(canvas, graphics, eastings, northings, 2);
		} else if (interval == 100) {
			renderLabels(canvas, graphics, eastings, northings, 3);
		} else if (interval == 10) {
			renderLabels(canvas, graphics, eastings, northings, 4);
		}
	}

	private MGRSOverlay() {
	}

	public static MGRSOverlay getInstance() {
		if (instance == null) {
			instance = new MGRSOverlay();
		}
		return instance;
	}

	private BufferedImage lastImage;
	private double[] lastBounds;
	@Override
	protected void onDraw(MapCanvas canvas, Graphics2D graphics) {
		App app = canvas.getApp();
		Viewport vp = app.getViewport();
		double[] bounds = vp.getBounds();
		if (lastImage != null && lastBounds != null) { // Check for reuse.
			boolean same = true;
			for (int i = 0; i < 4; i++) {
				if (lastBounds[i] != bounds[i]) {
					same = false;
				}
			}
			if (same) {
				graphics.drawImage(lastImage, 0, 0, null);
				return;
			}
		}

		int zoom = canvas.getMapPosition().getZoom();
		MGRSViewport lines = null;
		if (zoom > 2) {
			lines = new MGRSViewport(vp);
		} else {
			return;
		}

		// Render to an image for performance.
		int height = canvas.getHeight();
		int width = canvas.getWidth();
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gimg = bi.createGraphics();

		for (GeoRectangle rec : lines.getGeoExtents()) {
			if (zoom > 19) {
				renderGridCell(canvas, gimg, rec, 10);
			} else if (zoom > 15) {
				renderGridCell(canvas, gimg, rec, 100);
			} else if (zoom > 11) {
				renderGridCell(canvas, gimg, rec, 1000);
			} else if (zoom > 9) {
				renderGridCell(canvas, gimg, rec, 10000);
			}
			if (zoom > 5) {
				renderGridCell(canvas, gimg, rec, 100000);
			}
			if (zoom < 10) {
				MGRSPoint p = rec.getMGRSCenter();
				String label = p.getZone();
				int y = canvas.getMapPosition().getYFromLat(
						rec.getCenter().getLatitude(), height);
				int x = canvas.getMapPosition().getXFromLong(
						rec.getCenter().getLongitude(), width);
				FontMetrics metrics = gimg.getFontMetrics();
				drawString(gimg, label,
						x - (metrics.stringWidth(label) / 2), y);
			}
		}
		renderMGRSGrid(canvas, gimg, lines);
		graphics.drawImage(bi, 0, 0, null);
		lastImage = bi;
		lastBounds = bounds;
	}
}
