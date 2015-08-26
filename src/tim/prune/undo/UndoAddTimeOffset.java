package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.TrackInfo;

/**
 * Undo addition/subtraction of a time offset
 */
public class UndoAddTimeOffset implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** time offset */
	private long _timeOffset;


	/**
	 * Constructor
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 * @param inOffset time offset
	 */
	public UndoAddTimeOffset(int inStart, int inEnd, long inOffset)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
		_timeOffset = inOffset;
	}


	/**
	 * @return description of operation including number of points adjusted
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.addtimeoffset") + " (" + (_endIndex - _startIndex + 1) + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo, long timeOffset) throws UndoException
	{
		inTrackInfo.getTrack().addTimeOffset(_startIndex, _endIndex, timeOffset, true);
		UpdateMessageBroker.informSubscribers();
	}
	
	public void performUndo( App app ) throws UndoException {
		// Perform the inverse operation
		performUndo(((PruneApp) app).getTrackInfo(), -_timeOffset);
	}

	@Override
	public void performRedo(App app) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _timeOffset);
	}
}
