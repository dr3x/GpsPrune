package tim.prune.util;

import java.awt.geom.Point2D.Double;

import tim.prune.drawing.item.Ellipse;

/**
 * This class is a helper that provides calculations or services about properties of ellipsoids.
 * 
 * @author david
 *
 */
public final class EllipsoidHelper {

	
	public static final int DEGREES = 360;
	public static final int EARTH_CIRCUMFERENCE_IN_METERS = 40075160;
	
	private static final String ELLIPSOID_REQUIREMENT = "An Ellipse must have at least 2 points.";
	private static final String POSITIVE_DEGREES_REQUIREMENT = "Conversion from degrees to meters requires degrees > 0.";
	private static final String POSITIVE_METERS_REQUIREMENT = "Conversion from meters to degrees requires meters > 0.";

	
	public static double getMajorAxisInMeters(Ellipse ellipse) throws IllegalArgumentException {
		
		Double point1 = ellipse.getPoints().get(0);
		Double point2 = ellipse.getPoints().get(1);
		
		if (point1 == null || point2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		double xDisInDegrees = getXDistanceInDegrees(point1, point2);
		double yDisInDegrees = getYDistanceInDegrees(point1, point2);
		
		double xDisInMeters = degrees2Meters(xDisInDegrees);
		double yDisInMeters = degrees2Meters(yDisInDegrees);
		
		double majorAxis = (xDisInMeters > yDisInMeters) ? xDisInMeters : yDisInMeters;
		
		return majorAxis;
	}
	
	public static double getMinorAxisInMeters(Ellipse ellipse) throws IllegalArgumentException {
		
		Double point1 = ellipse.getPoints().get(0);
		Double point2 = ellipse.getPoints().get(1);
		
		if (point1 == null || point2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		double xDisInDegrees = getXDistanceInDegrees(point1, point2);
		double yDisInDegrees = getYDistanceInDegrees(point1, point2);
		
		double xDisInMeters = degrees2Meters(xDisInDegrees);
		double yDisInMeters = degrees2Meters(yDisInDegrees);
		
		double minorAxis = (xDisInMeters < yDisInMeters) ? xDisInMeters : yDisInMeters; 
		
		return minorAxis;
	}
	
	public static double getXDistanceInDegrees(Double p1, Double p2) throws IllegalArgumentException {
		
		if (p1 == null || p2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		return Math.abs(p2.x - p1.x);
	}
	
	public static double getYDistanceInDegrees(Double p1, Double p2) throws IllegalArgumentException {
		
		if (p1 == null || p2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		return Math.abs(p2.y - p1.y);
	}
	
	/**
	 * This method converts from degrees to meters using the circumference of the earth.  Note: in
	 * reality a degree at different places on the earth surface will yield different distances;
	 * whereas this method always yield the same value.
	 * 
	 * @param degrees
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static double degrees2Meters(double degrees) throws IllegalArgumentException {
		
		if (degrees < 0)
			throw new IllegalArgumentException(POSITIVE_DEGREES_REQUIREMENT);
		
		return degrees * (EARTH_CIRCUMFERENCE_IN_METERS / 360);
	}
	
	/**
	 * This method uses the circumference of the earth in meters
	 * 
	 * @param meters
	 * @return
	 */
	public static double metersToDegrees(double meters) {
		
		if (meters < 0)
			throw new IllegalArgumentException(POSITIVE_METERS_REQUIREMENT);
		
		return meters * DEGREES / EARTH_CIRCUMFERENCE_IN_METERS;
	}
	
	public static Double getTopLeft(Ellipse ellipse) throws IllegalArgumentException {
		
		Double point1 = ellipse.getPoints().get(0);
		Double point2 = ellipse.getPoints().get(1);
		
		if (point1 == null || point2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		Double topLeft = new Double();
		
		topLeft.x = (point1.x < point2.x) ? point1.x : point2.x;
		topLeft.y = (point1.y > point2.y) ? point1.y : point2.y;
		
		return topLeft;
	}
	
	public static Double getBottomRight(Ellipse ellipse) {
		
		Double point1 = ellipse.getPoints().get(0);
		Double point2 = ellipse.getPoints().get(1);
		
		if (point1 == null || point2 == null)
			throw new IllegalArgumentException(ELLIPSOID_REQUIREMENT);
		
		Double bottomRight = new Double();
		
		bottomRight.x = (point1.x > point2.x) ? point1.x : point2.x;
		bottomRight.y = (point1.y < point2.y) ? point1.y : point2.y;
		
		return bottomRight;
	}
}
