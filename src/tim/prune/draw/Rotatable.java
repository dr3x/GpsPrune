package tim.prune.draw;

import java.awt.geom.Point2D.Double;

public interface Rotatable {
	
	double getRotationAngle();
	
	Double getRotationPoint();
	
	/**
	 * Rotate some number of degrees from true north about point
	 * 
	 * @param angle
	 */
	void rotate( double angle, Double point );
}
