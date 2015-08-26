package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a range of points
 */
public class UndoDeleteRange implements UndoOperation
{
	private int _startIndex = -1;
	private int _endIndex = -1;
	private int _numToDelete = 0;
	private DataPoint[] _points = null;
	private DataPoint _nextTrackPoint = null;
	private boolean _segmentStart = false;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoDeleteRange(TrackInfo inTrackInfo)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		_endIndex = inTrackInfo.getSelection().getEnd();
		if (_startIndex >= 0 && _endIndex >= _startIndex) {
			_numToDelete = _endIndex - _startIndex + 1;
		}
		_points = inTrackInfo.cloneSelectedRange();
		// Save segment flag of following track point
		_nextTrackPoint = inTrackInfo.getTrack().getNextTrackPoint(_startIndex + _points.length);
		if (_nextTrackPoint != null) {
			_segmentStart = _nextTrackPoint.getSegmentStart();
		}
	}


	/**
	 * @return description of operation including range length
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.deleterange")
			+ " (" + _points.length + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo)
	{
		// restore point array into track
		inTrackInfo.getTrack().insertRange(_points, _startIndex);
		// Restore segment flag of following track point
		if (_nextTrackPoint != null) {
			_nextTrackPoint.setSegmentStart(_segmentStart);
		}
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		trackInfo.getSelection().selectRange(_startIndex, _endIndex);
		// call track to delete range
		if (trackInfo.deleteRange())
		{
			// Confirm
			UpdateMessageBroker.informSubscribers("" + _numToDelete + " "
				+ I18nManager.getText("confirm.deletepoint.multi"));
		}
	}
}