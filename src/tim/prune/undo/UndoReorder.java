package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Abstract operation to undo a reordering by replacing track contents with a shallow copy
 */
public abstract class UndoReorder implements UndoOperation
{
	/** Shallow copy of whole track contents */
	private DataPoint[] _contents = null;
	/** Shallow copy of whole track contents after data change */
	private DataPoint[] _redoContents = null;
	/** Description */
	private String _description = null;

	/**
	 * Constructor
	 * @param inTrack track contents to copy
	 * @param inDescKey description key
	 */
	public UndoReorder(Track inTrack, String inDescKey)
	{
		_contents = inTrack.cloneContents();
		_description = I18nManager.getText(inDescKey);
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return _description;
	}

	public void setRedoData(Track inTrack) {
		_redoContents = inTrack.cloneContents();
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo, DataPoint[] contents) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(contents);
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _contents);
	}

	@Override
	public void performRedo(App app) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _redoContents);
	}
}