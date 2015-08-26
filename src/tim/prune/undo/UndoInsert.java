package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an insertion (eg interpolate, average)
 */
public class UndoInsert implements UndoOperation
{
	private int _startPosition = 0;
	private int _numInserted = 0;
	private int _startIndex = 0;
	private int _endIndex = 0;
	private boolean _hasSegmentFlag = false;
	private boolean _segmentFlag = false;
	private boolean _isAverage = false;


	/**
	 * Constructor without segment flag
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 */
	public UndoInsert(TrackInfo trackInfo, int inStart, int inNumInserted)
	{
		this(trackInfo, inStart, inNumInserted, false, false, false);
	}

	/**
	 * Constructor
	 * @param inStart start of insert
	 * @param inNumInserted number of points inserted
	 * @param inHasFlag is there a segment flag present
	 * @param inFlag segment flag, if any
	 */
	public UndoInsert(TrackInfo trackInfo, int inStart, int inNumInserted, 
			boolean inHasFlag, boolean inFlag, boolean isAverage)
	{
		_startPosition = inStart;
		_numInserted = inNumInserted;
		_hasSegmentFlag = inHasFlag;
		_segmentFlag = inFlag;
		_isAverage = isAverage;
		_startIndex = trackInfo.getSelection().getStart();
		_endIndex = trackInfo.getSelection().getEnd();
	}


	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.insert") + " (" + _numInserted + ")";
	}


	/**
	 * Perform the undo operation on the given TrackInfo
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().deleteRange(_startPosition, _startPosition + _numInserted - 1);
		if (_hasSegmentFlag) {
			DataPoint nextPoint = inTrackInfo.getTrack().getNextTrackPoint(_startPosition);
			if (nextPoint != null) {nextPoint.setSegmentStart(_segmentFlag);}
		}
		// reset selection
		inTrackInfo.getSelection().clearAll();
	}

	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		trackInfo.getSelection().selectRange(_startIndex, _endIndex);
		if (_isAverage) {
			trackInfo.average();
		} else {
			trackInfo.interpolate(_numInserted);
		}
	}
}