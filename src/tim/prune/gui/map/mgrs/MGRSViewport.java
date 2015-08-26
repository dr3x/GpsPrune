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

import java.util.ArrayList;
import java.util.List;

import tim.prune.gui.Viewport;

public class MGRSViewport {
	private final List<GeoRectangle> geoRectangles;
	private final List<Double> latCoords;
	private final List<Double> lngCoords;

	public MGRSViewport(Viewport viewport) {
		double[] bounds = viewport.getBounds();
		double slat = bounds[0];
		double wlng = bounds[1];
		double nlat = bounds[2];
		double elng = bounds[3];
		latCoords = new ArrayList<Double>();
		lngCoords = new ArrayList<Double>();
		geoRectangles = new ArrayList<GeoRectangle>();
		// UTM is undefined beyond 84N or 80S, so this application defines viewport at those limits
		if (nlat > 84) nlat=84;
		// first zone intersection inside the southwest corner of the map window
		// longitude coordinate is straight-forward...
		float x1 = (float) (Math.floor((wlng/6)+1)*6.0);
		float y1;
		// but latitude coordinate has three cases
		if (slat < -80) {  // far southern zone; limit of UTM definition
			y1 = -80;
		} else { 
			y1 = (float) (Math.floor((slat/8)+1)*8.0);
		}

		// compute lines of UTM zones -- geographic lines at 6x8 deg intervals
		// compute the latitude coordinates that belong to this viewport
		if (slat < -80) latCoords.add(-80.0);  // special case of southern limit
		else latCoords.add(0, slat);  // normal case
		for (double lat=y1; lat < nlat; lat+=8) {
			if (lat <= 72) {
				latCoords.add(lat);
			} else if (lat <= 80) {
				latCoords.add(84.0);
			}
		}
		latCoords.add(nlat);

		// compute the longitude coordinates that belong to this viewport
		lngCoords.add(0, wlng);
		if (wlng < elng) {   // normal case
			for (double lng=x1; lng < elng; lng+=6) {
				lngCoords.add(lng);
			}
		} else { // special case of window that includes the international dateline
			for (double lng=x1; lng <= 180; lng+=6) {
				lngCoords.add(lng);
			}
			for (double lng=-180; lng < elng; lng+=6) {
				lngCoords.add(lng);
			}
		}
		lngCoords.add(elng);

		// store corners and center point for each geographic rectangle in the viewport
		// each rectangle may be a full UTM cell, but more commonly will have one or more
		// edges bounded by the extent of the viewport
		for (int i=0; i < latCoords.size()-1; i++) {
			for (int j=0; j < lngCoords.size()-1; j++) {
				if (latCoords.get(i)>=72 && lngCoords.get(j)==6) {  } // do nothing
				else if (latCoords.get(i)>=72 && lngCoords.get(j)==18) {  } // do nothing
				else if (latCoords.get(i)>=72 && lngCoords.get(j)==30) {  } // do nothing
				else {
					geoRectangles.add(new GeoRectangle(latCoords.get(i),
							lngCoords.get(j),
							latCoords.get(i+1),
							lngCoords.get(j+1)));
//					if (this.lat_coords[i] != thixs.lat_coords[i+1]) {  // ignore special case of -80 deg latitude
//						this.georectangle[k].assignCenter()
//					}
				}
			}
		}
	}

	public List<GeoRectangle> getGeoExtents() {
		return geoRectangles;
	}

	// return array of latitude coordinates corresponding to lat lines
	public final List<Double> getLats() {
		return latCoords;
	}

	// return array of longitude coordinates corresponding to lng lines
	public final List<Double> getLngs() {
		return lngCoords;
	}
}
