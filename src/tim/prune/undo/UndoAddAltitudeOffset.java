package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Undo addition/subtraction of an altitude offset
 */
public class UndoAddAltitudeOffset implements UndoOperation
{
	/** Start index of section */
	private int _startIndex;
	/** altitude values before operation */
	private Altitude[] _altitudes;
	private Altitude[] _redoAltitudes;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoAddAltitudeOffset(TrackInfo inTrackInfo)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		final int endIndex = inTrackInfo.getSelection().getEnd();
		final int numPoints = endIndex - _startIndex + 1;
		// Make array of cloned altitude objects
		_altitudes = new Altitude[numPoints];
		for (int i=0; i<numPoints; i++) {
			Altitude a = inTrackInfo.getTrack().getPoint(_startIndex+i).getAltitude();
			if (a != null && a.isValid()) {
				_altitudes[i] = a.clone();
			}
		}
	}


	/**
	 * @return description of operation including number of points adjusted
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.addaltitudeoffset") + " (" + (_altitudes.length) + ")";
	}

	/**
	 * Record data after set for redo.
	 * @param inTrackInfo track info object
	 */
	public void setRedoAltitudes(TrackInfo inTrackInfo)
	{
		final int endIndex = inTrackInfo.getSelection().getEnd();
		final int numPoints = endIndex - _startIndex + 1;
		// Make array of cloned altitude objects
		_redoAltitudes = new Altitude[numPoints];
		for (int i=0; i<numPoints; i++) {
			Altitude a = inTrackInfo.getTrack().getPoint(_startIndex+i).getAltitude();
			if (a != null && a.isValid()) {
				_redoAltitudes[i] = a.clone();
			}
		}
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 * @param altitudes Array of altitude data to reset to
	 */
	public void performUndo(TrackInfo inTrackInfo, Altitude[] altitudes) throws UndoException
	{
		// Perform the inverse operation
		final int numPoints = altitudes.length;
		for (int i=0; i<numPoints; i++) {
			DataPoint point = inTrackInfo.getTrack().getPoint(i+_startIndex);
			point.getAltitude().reset(altitudes[i]);
			point.setModified(true);
		}
		inTrackInfo.getSelection().markInvalid();
		UpdateMessageBroker.informSubscribers();
	}

	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _altitudes);
	}

	@Override
	public void performRedo(App app) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _redoAltitudes);
	}
}
