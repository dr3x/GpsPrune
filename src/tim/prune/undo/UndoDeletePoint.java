package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a single point
 */
public class UndoDeletePoint implements UndoOperation
{
	private int _pointIndex = -1;
	private DataPoint _point = null;
	private boolean _segmentStart = false;


	/**
	 * Constructor
	 * @param inPointIndex index number of point within track
	 * @param inPoint data point
	 * @param inPhotoIndex index number of photo within photo list
	 * @param inSegmentStart true if following track point starts new segment
	 */
	public UndoDeletePoint(int inPointIndex, DataPoint inPoint, 
			boolean inSegmentStart)
	{
		_pointIndex = inPointIndex;
		_point = inPoint;
		_segmentStart = inSegmentStart;
	}


	/**
	 * @return description of operation including point name if any
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.deletepoint");
		String pointName = _point.getWaypointName();
		if (pointName != null && !pointName.equals(""))
			desc = desc + " " + pointName;
		return desc;
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore point into track
		if (!inTrackInfo.getTrack().insertPoint(_point, _pointIndex))
		{
			throw new UndoException(getDescription());
		}
		// Restore previous status of following track point if necessary
		if (!_segmentStart)
		{
			// Deletion of point can only set following point to true, so only need to set it back to false
			DataPoint nextTrackPoint = inTrackInfo.getTrack().getNextTrackPoint(_pointIndex + 1);
			if (nextTrackPoint != null) {
				nextTrackPoint.setSegmentStart(false);
			}
		}
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		if (_point != null)
		{
			trackInfo.selectPoint(_point);
			// call track to delete point
			if (trackInfo.deletePoint())
			{
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.deletepoint.single"));
			}
		}
	}
}
