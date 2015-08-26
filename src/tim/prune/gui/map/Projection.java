package tim.prune.gui.map;

import java.awt.geom.Point2D;


public class Projection {
	
	private final MapPosition position;
	private final MapCanvas canvas;
	
	public Projection(MapPosition position, MapCanvas canvas) {
		this.position = position;
		this.canvas = canvas;
	}

	public Point2D.Double toScreen( double lat, double lon, Point2D.Double reuse ) {
		reuse = reuse == null ? new Point2D.Double() : reuse;
		reuse.x = canvas.getWidth() / 2 + position.getXFromCentre(MapUtils.getXFromLongitude(lon));
		reuse.y = canvas.getHeight() / 2 + position.getYFromCentre(MapUtils.getYFromLatitude(lat));
		return reuse;
	}
	
	public Point2D.Double fromScreen( int x, int y, Point2D.Double reuse ) {
		reuse = reuse == null ? new Point2D.Double() : reuse;
		reuse.y = MapUtils.getLatitudeFromY(position.getYFromPixels(y, canvas.getHeight()));
		reuse.x = MapUtils.getLongitudeFromX(position.getXFromPixels(x, canvas.getWidth()));
		return reuse;
	}
}
