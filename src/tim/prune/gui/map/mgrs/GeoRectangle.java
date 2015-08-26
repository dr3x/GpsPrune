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


import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

public class GeoRectangle {
	private final LatLonPoint swCorner;
	private final LatLonPoint neCorner;
	private final LatLonPoint center;
	private final MGRSPoint mgrsCenter;

	private double normLng(double lng) {
		if (lng > 180) lng = -180 + (lng - 180);
		if (lng < -180) lng = 180 + (lng + 180);
		if (lng == -180) lng = 180;
		return lng;
	}

	public GeoRectangle(double slat, double wlng, double nlat, double elng) {
		if (slat==56 && wlng==0) { // special case: Norway
			elng = elng-3;
		} else if (slat==56 && wlng==6) {
			wlng = wlng-3;
		} else if (slat==72 && wlng==0) { // special case: Svlabard
			elng = elng+3;
		} else if (slat==72 && wlng==12) {
			wlng = wlng-3;
			elng = elng+3;
		} else if (slat==72 && wlng==36) {
			wlng = wlng-3;
		}
		this.center = new LatLonPoint((nlat+slat)/2.0, normLng((wlng+elng)/2.0));
		if (wlng == -180) wlng = -179.999999999;
		else if (wlng == 180) wlng = -179.999999999;
		if (nlat == 0) nlat = -0.000000000000001;
		this.swCorner = new LatLonPoint(slat, normLng(wlng));
		this.neCorner = new LatLonPoint(nlat, normLng(elng));
		this.mgrsCenter = new MGRSPoint(this.center);
	}

	public LatLonPoint getCenter() {
		return center;
	}
	public MGRSPoint getMGRSCenter() {
		return mgrsCenter;
	}

	public LatLonPoint getSW() {
		return swCorner;
	}

	public LatLonPoint getNE() {
		return neCorner;
	}
}
