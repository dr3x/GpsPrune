package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo lookup of altitudes from SRTM data
 */
public class UndoLookupSrtm implements UndoOperation
{
	/** DataPoint objects which didn't have altitudes before */
	private DataPoint[] _points;
	/** Altitude strings if present */
	private String[] _altitudes;
	/** Altitude strings for redo */
	private String[] _redoAltitudes;

	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoLookupSrtm(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo.getTrack();
		int numPoints = track.getNumPoints();
		// Make arrays of points and altitudes
		_points = new DataPoint[numPoints];
		_altitudes = new String[numPoints];
		for (int i=0; i<numPoints; i++) {
			DataPoint point = track.getPoint(i);
			if (!point.hasAltitude() || point.getAltitude().getValue() == 0) {
				_points[i] = point;
				if (point.hasAltitude()) {
					_altitudes[i] = point.getFieldValue(Field.ALTITUDE);
				}
			}
		}
	}

	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.lookupsrtm");
	}

	public void setRedoData(TrackInfo inTrackInfo) {
		Track track = inTrackInfo.getTrack();
		int numPoints = track.getNumPoints();
		_redoAltitudes = new String[numPoints];
		for (int i=0; i<numPoints; i++) {
			DataPoint point = track.getPoint(i);
			if (point.hasAltitude()) {
					_redoAltitudes[i] = point.getFieldValue(Field.ALTITUDE);
			}
		}
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Loop through points again, and reset altitudes if they have one
		final int numPoints = _points.length;
		for (int i=0; i<numPoints; i++) {
			DataPoint point = _points[i];
			if (point != null && point.hasAltitude()) {
				if (_altitudes[i] == null) {
					point.setFieldValue(Field.ALTITUDE, null, true);
				}
				else {
					point.setFieldValue(Field.ALTITUDE, _altitudes[i], true);
				}
			}
		}
		UpdateMessageBroker.informSubscribers();
	}

	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		final int numPoints = _points.length;
		int numAltitudesFound = 0;
		for (int i=0; i<numPoints; i++) {
			DataPoint point = _points[i];
			if (point != null) {
				if (_redoAltitudes[i] == null) {
					point.setFieldValue(Field.ALTITUDE, null, true);
				}
				else {
					point.setFieldValue(Field.ALTITUDE, _redoAltitudes[i], true);
					numAltitudesFound++;
				}
			}
		}
		((PruneApp) app).completeFunction(null, I18nManager.getText("confirm.lookupsrtm1") + " " + numAltitudesFound
				+ " " + I18nManager.getText("confirm.lookupsrtm2"));
		UpdateMessageBroker.informSubscribers();
	}
}
