package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo merging of track segments
 */
public class UndoMergeTrackSegments implements UndoOperation
{
	/** Start index */
	private int _startIndex;
	/** End index */
	private int _endIndex;
	/** array of segment flags */
	private boolean[] _segmentFlags = null;
	/** Following point, if any */
	private DataPoint _nextTrackPoint = null;
	/** Segment flag of next point */
	private boolean _nextSegmentFlag = false;


	/**
	 * Constructor
	 * @param inTrack track object for copying segment flags
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 */
	public UndoMergeTrackSegments(Track inTrack, int inStart, int inEnd)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
		// Store booleans for all points within selection
		int numPoints = inEnd - inStart + 1;
		_segmentFlags = new boolean[numPoints];
		for (int i=inStart; i<=inEnd; i++) {
			_segmentFlags[i-inStart] = inTrack.getPoint(i).getSegmentStart();
		}
		// Look for following track point, store flag
		_nextTrackPoint = inTrack.getNextTrackPoint(inEnd + 1);
		if (_nextTrackPoint != null) {
			_nextSegmentFlag = _nextTrackPoint.getSegmentStart();
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.mergetracksegments");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Loop through points replacing segment start flags
		for (int i=0; i<_segmentFlags.length; i++) {
			DataPoint point = inTrackInfo.getTrack().getPoint(_startIndex + i);
			if (!point.isWaypoint()) {
				point.setSegmentStart(_segmentFlags[i]);
			}
		}
		// Restore segment start flag for following point
		if (_nextTrackPoint != null) {
			_nextTrackPoint.setSegmentStart(_nextSegmentFlag);
		}
		UpdateMessageBroker.informSubscribers();
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		// Call track to merge segments
		if (trackInfo.mergeTrackSegments(_startIndex, _endIndex)) {
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.mergetracksegments"));
		}
	}
}
