package tim.prune.data;

import tim.prune.UpdateMessageBroker;

/**
 * Class to hold all track information, including data
 * and the selection information
 */
public class TrackInfo
{
	private Track _track = null;
	private Selection _selection = null;
	private FileInfo _fileInfo = null;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public TrackInfo(Track inTrack)
	{
		_track = inTrack;
		_selection = new Selection(_track);
		_fileInfo = new FileInfo();
	}


	/**
	 * @return the Track object
	 */
	public Track getTrack() {
		return _track;
	}


	/**
	 * @return the Selection object
	 */
	public Selection getSelection() {
		return _selection;
	}


	/**
	 * @return the FileInfo object
	 */
	public FileInfo getFileInfo() {
		return _fileInfo;
	}

	/**
	 * Replace the file info with a previously made clone
	 * @param inInfo cloned file info
	 */
	public void setFileInfo(FileInfo inInfo) {
		_fileInfo = inInfo;
	}

	/**
	 * Get the currently selected point, if any
	 * @return DataPoint if single point selected, otherwise null
	 */
	public DataPoint getCurrentPoint() {
		return _track.getPoint(_selection.getCurrentPointIndex());
	}

	/**
	 * Delete the currently selected range of points
	 * @return true if successful
	 */
	public boolean deleteRange()
	{
		int startSel = _selection.getStart();
		int endSel = _selection.getEnd();
		boolean answer = _track.deleteRange(startSel, endSel);
		// clear range selection
		_selection.modifyRangeDeleted();
		return answer;
	}


	/**
	 * Delete the currently selected point
	 * @return true if point deleted
	 */
	public boolean deletePoint()
	{
		if (_track.deletePoint(_selection.getCurrentPointIndex()))
		{
			_selection.modifyPointDeleted();
			UpdateMessageBroker.informSubscribers();
			return true;
		}
		return false;
	}


	/**
	 * Delete all the points which have been marked for deletion
	 * @return number of points deleted
	 */
	public int deleteMarkedPoints()
	{
		int numDeleted = _track.deleteMarkedPoints();
		if (numDeleted > 0) {
			_selection.clearAll();
			UpdateMessageBroker.informSubscribers();
		}
		return numDeleted;
	}


	/**
	 * Clone the selected range of data points
	 * @return shallow copy of DataPoint objects
	 */
	public DataPoint[] cloneSelectedRange()
	{
		return _track.cloneRange(_selection.getStart(), _selection.getEnd());
	}

	/**
	 * Merge the track segments within the given range
	 * @param inStart start index
	 * @param inEnd end index
	 * @return true if successful
	 */
	public boolean mergeTrackSegments(int inStart, int inEnd)
	{
		boolean firstTrackPoint = true;
		// Loop between start and end
		for (int i=inStart; i<=inEnd; i++) {
			DataPoint point = _track.getPoint(i);
			// Set all segments to false apart from first track point
			if (point != null && !point.isWaypoint()) {
				point.setSegmentStart(firstTrackPoint);
				firstTrackPoint = false;
			}
		}
		// Find following track point, if any
		DataPoint nextPoint = _track.getNextTrackPoint(inEnd+1);
		if (nextPoint != null) {nextPoint.setSegmentStart(true);}
		_selection.markInvalid();
		UpdateMessageBroker.informSubscribers();
		return true;
	}

	/**
	 * Interpolate extra points between two selected ones
	 * @param inNumPoints num points to insert
	 * @return true if successful
	 */
	public boolean interpolate(int inNumPoints)
	{
		boolean success = _track.interpolate(_selection.getStart(), inNumPoints);
		if (success) {
			_selection.selectRangeEnd(_selection.getEnd() + inNumPoints);
		}
		return success;
	}


	/**
	 * Average selected points to create a new one
	 * @return true if successful
	 */
	public boolean average()
	{
		boolean success = _track.average(_selection.getStart(), _selection.getEnd());
		if (success) {
			selectPoint(_selection.getEnd()+1);
		}
		return success;
	}


	/**
	 * Select the given DataPoint
	 * @param inPoint DataPoint object to select
	 */
	public void selectPoint(DataPoint inPoint)
	{
		selectPoint(_track.getPointIndex(inPoint));
	}

	/**
	 * Select the data point with the given index
	 * @param inPointIndex index of DataPoint to select, or -1 for none
	 */
	public void selectPoint(int inPointIndex)
	{
		if (_selection.getCurrentPointIndex() == inPointIndex || inPointIndex >= _track.getNumPoints()) {return;}
		// give to selection
		_selection.selectPoint(inPointIndex);
	}

	/**
	 * Extend the current selection to end at the given point, eg by shift-clicking
	 * @param inPointNum index of end point
	 */
	public void extendSelection(int inPointNum)
	{
		// See whether to start selection from current range start or current point
		int rangeStart = _selection.getStart();
		if (rangeStart < 0 || _selection.getCurrentPointIndex() != _selection.getEnd()) {
			rangeStart = _selection.getCurrentPointIndex();
		}
		selectPoint(inPointNum);
		if (rangeStart < inPointNum) {
			_selection.selectRange(rangeStart, inPointNum);
		}
	}
}
