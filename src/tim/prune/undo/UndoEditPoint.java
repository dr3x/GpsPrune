package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;
import tim.prune.function.edit.FieldEditList;

/**
 * Operation to undo the edit of a single point
 */
public class UndoEditPoint implements UndoOperation
{
	private DataPoint _originalPoint = null;
	private FieldEditList _undoFieldList = null;
	private FieldEditList _editList = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inUndoFieldList FieldEditList for undo operation
	 */
	public UndoEditPoint(DataPoint inPoint, FieldEditList inUndoFieldList, FieldEditList inEditList)
	{
		_originalPoint = inPoint;
		_undoFieldList = inUndoFieldList;
		_editList = inEditList;
	}


	/**
	 * @return description of operation including point name if any
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.editpoint");
		String newName = _undoFieldList.getEdit(0).getValue();
		String pointName = _originalPoint.getWaypointName();
		if (newName != null && !newName.equals(""))
			desc = desc + " " + newName;
		else if (pointName != null && !pointName.equals(""))
			desc = desc + " " + pointName;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo, FieldEditList editList) throws UndoException
	{
		// Restore contents of point into track
		if (!inTrackInfo.getTrack().editPoint(_originalPoint, editList, true))
		{
			// throw exception if failed
			throw new UndoException(getDescription());
		}
	}
	
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _undoFieldList);
	}

	@Override
	public void performRedo(App app) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo(), _editList);
	}
}