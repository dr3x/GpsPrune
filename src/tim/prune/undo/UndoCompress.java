package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a track compression
 */
public class UndoCompress implements UndoOperation
{
	private DataPoint[] _contents = null;
	private DataPoint[] _redoContents = null;
	protected int _numPointsDeleted = -1;
	private boolean[] _segmentStarts = null;


	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 */
	public UndoCompress(Track inTrack)
	{
		_contents = inTrack.cloneContents();
		// Copy boolean segment start flags
		_segmentStarts = new boolean[inTrack.getNumPoints()];
		for (int i=0; i<inTrack.getNumPoints(); i++) {
			_segmentStarts[i] = inTrack.getPoint(i).getSegmentStart();
		}
	}


	/**
	 * Set the number of points deleted
	 * (only known after attempted compression)
	 * @param inNum number of points deleted
	 */
	public void setNumPointsDeleted(int inNum)
	{
		_numPointsDeleted = inNum;
	}

	public void setRedoData(Track inTrack) {
		_redoContents = inTrack.cloneContents();
	}

	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.compress");
		if (_numPointsDeleted > 0)
			desc = desc + " (" + _numPointsDeleted + ")";
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
		// Copy boolean segment start flags
		Track track = inTrackInfo.getTrack();
		if (_segmentStarts.length != track.getNumPoints())
			throw new UndoException("Cannot undo compress - track length no longer matches");
		for (int i=0; i<_segmentStarts.length; i++) {
			track.getPoint(i).setSegmentStart(_segmentStarts[i]);
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		((PruneApp) app).getTrackInfo().getTrack().replaceContents(_redoContents);
		UpdateMessageBroker.informSubscribers("" + _numPointsDeleted + " "
				 + (_numPointsDeleted==1?I18nManager.getText("confirm.deletepoint.single"):I18nManager.getText("confirm.deletepoint.multi")));
	}
}