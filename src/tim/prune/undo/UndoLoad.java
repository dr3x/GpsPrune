package tim.prune.undo;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.FileInfo;
import tim.prune.data.SourceInfo;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load operation
 */
public class UndoLoad implements UndoOperation
{
	public final static int LOAD    = 0;
	public final static int APPEND  = 1;
	public final static int REPLACE = 2;
	
	private int _operation = LOAD;
	private int _cropIndex = -1;
	private int _numLoaded = -1;
	private DataPoint[] _contents = null;
	private FileInfo _oldFileInfo = null;
	private Track _loadedTrack;
	private SourceInfo _sourceInfo;


	/**
	 * Constructor for appending
	 * @param inIndex index number of crop point
	 * @param inNumLoaded number of points loaded
	 */
	public UndoLoad(Track inLoadedTrack, SourceInfo inSourceInfo, 
			int inIndex, int inNumLoaded)
	{
		_cropIndex = inIndex;
		_numLoaded = inNumLoaded;
		_contents = null;
		_operation = APPEND;
		_loadedTrack = inLoadedTrack;
		_sourceInfo = inSourceInfo;
	}


	/**
	 * Constructor for replacing
	 * @param inOldTrackInfo track info being replaced
	 * @param inNumLoaded number of points loaded
	 * @param inPhotoList photo list, if any
	 */
	public UndoLoad(Track inLoadedTrack, SourceInfo inSourceInfo, 
			TrackInfo inOldTrackInfo, int inNumLoaded,
			int operation)
	{
		_cropIndex = -1;
		_numLoaded = inNumLoaded;
		_contents = inOldTrackInfo.getTrack().cloneContents();
		_oldFileInfo = inOldTrackInfo.getFileInfo().clone();
		_operation = operation;
		_loadedTrack = inLoadedTrack;
		_sourceInfo = inSourceInfo;
	}


	/**
	 * @return description of operation including number of points loaded
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.load");
		if (_numLoaded > 0)
			desc = desc + " (" + _numLoaded + ")";
		return desc;
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// remove source from fileinfo
		if (_oldFileInfo == null) {
			inTrackInfo.getFileInfo().removeSource();
		}
		else {
			inTrackInfo.setFileInfo(_oldFileInfo);
		}
		// Crop / replace
		if (_contents == null)
		{
			// crop track to previous size
			inTrackInfo.getTrack().cropTo(_cropIndex);
		}
		else
		{
			// replace track contents with old
			if (!inTrackInfo.getTrack().replaceContents(_contents))
			{
				throw new UndoException(getDescription());
			}
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
	
	public void performUndo( App app ) throws UndoException {
		performUndo(((PruneApp) app).getTrackInfo());
	}

	@Override
	public void performRedo(App app) throws UndoException {
		TrackInfo trackInfo = ((PruneApp) app).getTrackInfo();
		Track track = trackInfo.getTrack();
		if (_operation == APPEND) {
			track.combine(_loadedTrack);
			// set source information
			_sourceInfo.populatePointObjects(track, _loadedTrack.getNumPoints());
			trackInfo.getFileInfo().addSource(_sourceInfo);
			
		} else {
			trackInfo.getSelection().clearAll();
			track.load(_loadedTrack);
			_sourceInfo.populatePointObjects(track, track.getNumPoints());
			trackInfo.getFileInfo().replaceSource(_sourceInfo);
		}
		UpdateMessageBroker.informSubscribers();
		// Update status bar
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.loadfile")
			+ " '" + _sourceInfo.getName() + "'");
	}
}
