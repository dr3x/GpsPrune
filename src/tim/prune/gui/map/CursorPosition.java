package tim.prune.gui.map;

import java.awt.Point;
import java.awt.geom.Point2D.Double;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

public class CursorPosition extends MapLabel {
	private static final long serialVersionUID = 1L;


	private final MapCanvas canvas;
	private final MGRSPoint mgrsPoint;
	private final LatLonPoint llp;

	public CursorPosition(MapCanvas canvas, int align) {
		super(align);
		this.canvas = canvas;
		this.mgrsPoint = new MGRSPoint();
		this.llp = new LatLonPoint();
	}

	@Override
	protected String getText() {
		Projection projection = canvas.getProjection();
		String prettyMGRS = "";

		if( projection != null ) {
			Point mp = canvas.getMousePosition();
			if( mp != null ) {
				Double fromScreen = projection.fromScreen(mp.x, mp.y, null);
				llp.setLatLon(fromScreen.y, fromScreen.x);
				MGRSPoint.LLtoMGRS(llp, mgrsPoint);
				prettyMGRS = mgrsPoint.getPrettyMGRS();
			}
		}
		return prettyMGRS;
	}
}
