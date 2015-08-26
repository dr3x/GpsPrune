package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo creation of new point
 */
public class UndoCreatePoint implements UndoOperation
{
	private DataPoint _redoPoint = null;

	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.createpoint");
	}

	/**
	 * Sets the redoPoint for in case of redo.
	 * @param redoPoint Point to re-add on redo.
	 */
	public void setRedoPoint(DataPoint redoPoint) {
		_redoPoint = redoPoint;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		if (inTrackInfo.getTrack().getNumPoints() < 1)
		{
			throw new UndoException(getDescription());
		}
		// Reset selection if last point selected
		if (inTrackInfo.getSelection().getCurrentPointIndex() == (inTrackInfo.getTrack().getNumPoints()-1)) {
			inTrackInfo.getSelection().clearAll(); // Note: Informers told twice now!
		}
		// Remove last point
		inTrackInfo.getTrack().cropTo(inTrackInfo.getTrack().getNumPoints() - 1);
	}

	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		if (_redoPoint == null)
		{
			throw new UndoException(getDescription());
		}
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		Track track = trackInfo.getTrack();
		track.appendPoints(new DataPoint[] {_redoPoint});
		// ensure track's field list contains point's fields
		track.extendFieldList(_redoPoint.getFieldList());
		trackInfo.selectPoint(trackInfo.getTrack().getNumPoints()-1);
		// update listeners
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.createpoint"));
	}
}
