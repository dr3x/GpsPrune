package tim.prune;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FileInfo;
import tim.prune.data.NumberUtils;
import tim.prune.data.SourceInfo;
import tim.prune.data.Track;
import tim.prune.data.TrackFileInfo;
import tim.prune.data.TrackInfo;
import tim.prune.function.SelectTracksFunction;
import tim.prune.function.browser.BrowserLauncher;
import tim.prune.function.browser.UrlGenerator;
import tim.prune.function.edit.FieldEditList;
import tim.prune.function.edit.PointEditor;
import tim.prune.load.GenericFileFilter;
import tim.prune.load.TrackNameList;
import tim.prune.load.xml.GpxMetadata;
import tim.prune.save.GpxExporter;
import tim.prune.save.TrackFileSaver;
import tim.prune.undo.UndoAddAltitudeOffset;
import tim.prune.undo.UndoAddTimeOffset;
import tim.prune.undo.UndoCompress;
import tim.prune.undo.UndoCreatePoint;
import tim.prune.undo.UndoCutAndMove;
import tim.prune.undo.UndoDeletePoint;
import tim.prune.undo.UndoDeleteRange;
import tim.prune.undo.UndoEditPoint;
import tim.prune.undo.UndoInsert;
import tim.prune.undo.UndoLoad;
import tim.prune.undo.UndoMergeTrackSegments;
import tim.prune.undo.UndoOperation;
import tim.prune.undo.UndoReverseSection;

public class PruneApp extends App {

	private TrackFileSaver _fileSaver = null;
	private int _lastSavePosition = 0;
	private Track _track = null;
	private TrackInfo _trackInfo = null;
	private boolean _mangleTimestampsConfirmed = false;

	private Color[] trackColors = {
			Color.BLUE,
			Color.MAGENTA, 
			Color.ORANGE, 
			Color.CYAN, 
			Color.RED	
	};

	public PruneApp(JFrame inFrame) {
		super(inFrame, new File(Config.getConfigString(Config.KEY_BASE_FILE_LOCATION) + "/Honesty Traces"));
		_track = new Track();
		_trackInfo = new TrackInfo(_track);
		FunctionLibrary.initialise(this);
	}


	/**
	 * @return the current TrackInfo
	 */
	public TrackInfo getTrackInfo()
	{
		return _trackInfo;
	}

	@Override
	public boolean hasDataUnsaved() {
		return (getUndoStack().size() > _lastSavePosition
				&& (_track.getNumPoints() > 0));
	}


	/**
	 * Save the file in the selected format
	 */
	public void saveFile(boolean saveAs)
	{
		File selectedFile = getOpenFile();
		if(saveAs || selectedFile == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(getBaseDir(true));
			chooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
			chooser.setAcceptAllFileFilterUsed(false);
			if( selectedFile != null ) {
				chooser.setSelectedFile(selectedFile);
			}
			if( chooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION ) {
				selectedFile = chooser.getSelectedFile();
				if (!selectedFile.getName().endsWith(".gpx")) {
					selectedFile = new File(selectedFile.getPath()+".gpx");
				}
			}
		}

		if( selectedFile != null ) {
			OutputStreamWriter writer = null;
			try {
				if(selectedFile.getName().endsWith(".gpx")) {
					writer = new OutputStreamWriter(new FileOutputStream(selectedFile));
					final boolean[] saveFlags = {true, true, false, true };
					// write file
					GpxExporter.exportData(writer, _trackInfo, 
							_trackInfo.getTrack().getGpxMetadata().getName(),
							_trackInfo.getTrack().getGpxMetadata().getDesc(),
							saveFlags, false);
					setOpenFile(selectedFile);
					updateTitle(Collections.singletonList(selectedFile));
					informDataSaved();
				}
			} catch (Exception e) {
				showErrorMessageNoLookup("error.save.dialogtitle", "Failed to save file.  " + e.getMessage());
				e.printStackTrace();
			} finally {
				try { if (writer != null) writer.close(); } catch (IOException e) {}
			}
		}
	}

	/**
	 * Save the selected track into a gpx file.
	 */
	public void saveSelectionAs()
	{
		if (_trackInfo == null ||
				_trackInfo.getSelection() == null ||
				_trackInfo.getSelection().getStart() < 0 ||
				_trackInfo.getSelection().getEnd() < 0) {
			// No selection to save.
			JOptionPane.showMessageDialog(getFrame(),
					I18nManager.getText("dialog.norangetosave.text"));
			return;
		}
		File selectedFile = getOpenFile();
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getBaseDir(true));
		chooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
		if (selectedFile != null && selectedFile.getName().endsWith(".gpx")) {
			String name = selectedFile.getPath();
			name = name.substring(0, name.length() - 4);
			selectedFile = new File(name+"_"+
					(_trackInfo.getSelection().getStart()+1)+"to"+
					(_trackInfo.getSelection().getEnd()+1)+".gpx");
		}
		chooser.setAcceptAllFileFilterUsed(false);
		if( selectedFile != null ) {
			chooser.setSelectedFile(selectedFile);
		}
		selectedFile = null;
		if( chooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION ) {
			selectedFile = chooser.getSelectedFile();
			if (!selectedFile.getName().endsWith(".gpx")) {
				selectedFile = new File(selectedFile.getPath()+".gpx");
			}
		}

		if( selectedFile != null ) {
			OutputStreamWriter writer = null;
			try {
				if(selectedFile.getName().endsWith(".gpx")) {
					writer = new OutputStreamWriter(new FileOutputStream(selectedFile));
					final boolean[] saveFlags = {true, true, true, true };
					// write file
					GpxExporter.exportData(writer, _trackInfo, 
							_trackInfo.getTrack().getGpxMetadata().getName(),
							_trackInfo.getTrack().getGpxMetadata().getDesc(),
							saveFlags, false);
				}
			} catch (Exception e) {
				showErrorMessageNoLookup("error.save.dialogtitle", "Failed to save selection.  " + e.getMessage());
				e.printStackTrace();
			} finally {
				try { if (writer != null) writer.close(); } catch (IOException e) {}
			}
		}
	}

	public void exportAsText() {
		if (_fileSaver == null) {
			_fileSaver = new TrackFileSaver(this, getFrame());
		}
		char delim = ',';
		if (_fileLoader != null) {
			delim = _fileLoader.getLastUsedDelimiter();
		}
		_fileSaver.showDialog(delim);
	}

	/**
	 * Edit the currently selected point
	 */
	public void editCurrentPoint()
	{
		if (_track != null)
		{
			DataPoint currentPoint = _trackInfo.getCurrentPoint();
			if (currentPoint != null)
			{
				// Open point dialog to display details
				PointEditor editor = new PointEditor(this, getFrame());
				editor.showDialog(_track, currentPoint);
			}
		}
	}

	/**
	 * Complete the point edit
	 * @param inEditList field values to edit
	 * @param inUndoList field values before edit
	 */
	public void completePointEdit(FieldEditList inEditList, FieldEditList inUndoList)
	{
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		if (inEditList != null && inEditList.getNumEdits() > 0 && currentPoint != null)
		{
			// add information to undo stack
			UndoOperation undo = new UndoEditPoint(currentPoint, inUndoList, inEditList);
			// pass to track for completion
			if (_track.editPoint(currentPoint, inEditList, false))
			{
				getUndoStack().push(undo);
				// Confirm point edit
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.point.edit"));
			}
		}
	}


	/**
	 * Delete the currently selected point
	 */
	public void deleteCurrentPoint()
	{
		if (_track == null) {return;}
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		if (currentPoint != null)
		{
			// store necessary information to undo it later
			int pointIndex = _trackInfo.getSelection().getCurrentPointIndex();
			DataPoint nextTrackPoint = _trackInfo.getTrack().getNextTrackPoint(pointIndex + 1);
			// Construct Undo object
			UndoOperation undo = new UndoDeletePoint(pointIndex, currentPoint, 
					nextTrackPoint != null && nextTrackPoint.getSegmentStart());
			// call track to delete point
			if (_trackInfo.deletePoint())
			{
				// Delete was successful so add undo info to stack
				getUndoStack().push(undo);
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.deletepoint.single"));
			}
		}
	}


	/**
	 * Delete the currently selected range
	 */
	public void deleteSelectedRange()
	{
		if (_track != null)
		{
			// Find out if photos should be deleted or not
			int selStart = _trackInfo.getSelection().getStart();
			int selEnd = _trackInfo.getSelection().getEnd();
			if (selStart >= 0 && selEnd >= selStart)
			{
				int numToDelete = selEnd - selStart + 1;
				// add information to undo stack
				UndoDeleteRange undo = new UndoDeleteRange(_trackInfo);
				// call track to delete range
				if (_trackInfo.deleteRange())
				{
					getUndoStack().push(undo);
					// Confirm
					UpdateMessageBroker.informSubscribers("" + numToDelete + " "
							+ I18nManager.getText("confirm.deletepoint.multi"));
				}
			}
		}
	}


	/**
	 * Finish the compression by deleting the marked points
	 */
	public void finishCompressTrack()
	{
		UndoCompress undo = new UndoCompress(_track);
		// call track to do compress
		int numPointsDeleted = _trackInfo.deleteMarkedPoints();
		// add to undo stack if successful
		if (numPointsDeleted > 0)
		{
			undo.setNumPointsDeleted(numPointsDeleted);
			undo.setRedoData(_track);
			getUndoStack().add(undo);
			UpdateMessageBroker.informSubscribers("" + numPointsDeleted + " "
					+ (numPointsDeleted==1?I18nManager.getText("confirm.deletepoint.single"):I18nManager.getText("confirm.deletepoint.multi")));
		}
		else {
			showErrorMessage("function.compress", "dialog.compress.nonefound");
		}
	}

	/**
	 * Reverse the currently selected section of the track
	 */
	public void reverseRange()
	{
		// check whether Timestamp field exists, and if so confirm reversal
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		if (!_track.hasData(Field.TIMESTAMP, selStart, selEnd)
				|| _mangleTimestampsConfirmed
				|| (JOptionPane.showConfirmDialog(getFrame(),
						I18nManager.getText("dialog.confirmreversetrack.text"),
						I18nManager.getText("dialog.confirmreversetrack.title"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && (_mangleTimestampsConfirmed = true)))
		{
			UndoReverseSection undo = new UndoReverseSection(_track, selStart, selEnd);
			// call track to reverse range
			if (_track.reverseRange(selStart, selEnd))
			{
				getUndoStack().add(undo);
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.reverserange"));
			}
		}
	}

	/**
	 * Complete the add time offset function with the specified offset
	 * @param inTimeOffset time offset to add (+ve for add, -ve for subtract)
	 */
	public void finishAddTimeOffset(long inTimeOffset)
	{
		// Construct undo information
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		UndoAddTimeOffset undo = new UndoAddTimeOffset(selStart, selEnd, inTimeOffset);
		if (_trackInfo.getTrack().addTimeOffset(selStart, selEnd, inTimeOffset, false))
		{
			getUndoStack().add(undo);
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.addtimeoffset"));
		}
	}


	/**
	 * Complete the add altitude offset function with the specified offset
	 * @param inOffset altitude offset to add as String
	 * @param inFormat altitude format of offset (eg Feet, Metres)
	 */
	public void finishAddAltitudeOffset(String inOffset, Altitude.Format inFormat)
	{
		// Sanity check
		if (inOffset == null || inOffset.equals("") || inFormat==Altitude.Format.NO_FORMAT) {
			return;
		}
		// Construct undo information
		UndoAddAltitudeOffset undo = new UndoAddAltitudeOffset(_trackInfo);
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		// How many decimal places are given in the offset?
		int numDecimals = NumberUtils.getDecimalPlaces(inOffset);
		boolean success = false;
		// Decimal offset given
		try {
			double offsetd = Double.parseDouble(inOffset);
			success = _trackInfo.getTrack().addAltitudeOffset(selStart, selEnd, offsetd, inFormat, numDecimals);
		}
		catch (NumberFormatException nfe) {}
		if (success)
		{
			undo.setRedoAltitudes(_trackInfo);
			getUndoStack().add(undo);
			_trackInfo.getSelection().markInvalid();
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.addaltitudeoffset"));
		}
	}


	/**
	 * Merge the track segments within the current selection
	 */
	public void mergeTrackSegments()
	{
		if (_trackInfo.getSelection().hasRangeSelected())
		{
			// Maybe could check segment start flags to see if it's worth merging
			// If first track point is already start and no other seg starts then do nothing

			int selStart = _trackInfo.getSelection().getStart();
			int selEnd = _trackInfo.getSelection().getEnd();
			// Make undo object
			UndoMergeTrackSegments undo = new UndoMergeTrackSegments(_track, selStart, selEnd);
			// Call track to merge segments
			if (_trackInfo.mergeTrackSegments(selStart, selEnd)) {
				getUndoStack().add(undo);
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.mergetracksegments"));
			}
		}
	}


	/**
	 * Interpolate the two selected points
	 */
	public void interpolateSelection()
	{
		// Get number of points to add
		Object numPointsStr = JOptionPane.showInputDialog(getFrame(),
				I18nManager.getText("dialog.interpolate.parameter.text"),
				I18nManager.getText("dialog.interpolate.title"),
				JOptionPane.QUESTION_MESSAGE, null, null, "");
		int numPoints = parseNumber(numPointsStr);
		if (numPoints <= 0) return;

		UndoInsert undo = new UndoInsert(_trackInfo, _trackInfo.getSelection().getStart() + 1,
				numPoints);
		// call track to interpolate
		if (_trackInfo.interpolate(numPoints))
		{
			getUndoStack().add(undo);
		}
	}


	/**
	 * Average the selected points
	 */
	public void averageSelection()
	{
		// Find following track point
		DataPoint nextPoint = _track.getNextTrackPoint(_trackInfo.getSelection().getEnd() + 1);
		boolean segFlag = false;
		if (nextPoint != null) {segFlag = nextPoint.getSegmentStart();}
		UndoInsert undo = new UndoInsert(_trackInfo, _trackInfo.getSelection().getEnd() + 1,
				1, nextPoint != null, segFlag, true);
		// call track info object to do the averaging
		if (_trackInfo.average())
		{
			getUndoStack().add(undo);
		}
	}


	/**
	 * Create a new point at the given position
	 * @param inPoint point to add
	 */
	public void createPoint(DataPoint inPoint)
	{
		// create undo object
		UndoCreatePoint undo = new UndoCreatePoint();
		undo.setRedoPoint(inPoint);
		getUndoStack().add(undo);
		// add point to track
		inPoint.setSegmentStart(true);
		_track.appendPoints(new DataPoint[] {inPoint});
		// ensure track's field list contains point's fields
		_track.extendFieldList(inPoint.getFieldList());
		_trackInfo.selectPoint(_trackInfo.getTrack().getNumPoints()-1);
		// update listeners
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.createpoint"));
	}


	/**
	 * Cut the current selection and move it to before the currently selected point
	 */
	public void cutAndMoveSelection()
	{
		int startIndex = _trackInfo.getSelection().getStart();
		int endIndex = _trackInfo.getSelection().getEnd();
		int pointIndex = _trackInfo.getSelection().getCurrentPointIndex();
		// If timestamps would be mangled by cut/move, confirm
		if (!_track.hasData(Field.TIMESTAMP, startIndex, endIndex)
				|| _mangleTimestampsConfirmed
				|| (JOptionPane.showConfirmDialog(getFrame(),
						I18nManager.getText("dialog.confirmcutandmove.text"),
						I18nManager.getText("dialog.confirmcutandmove.title"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && (_mangleTimestampsConfirmed = true)))
		{
			// Find points to set segment flags
			DataPoint firstTrackPoint = _track.getNextTrackPoint(startIndex, endIndex);
			DataPoint nextTrackPoint = _track.getNextTrackPoint(endIndex+1);
			DataPoint moveToTrackPoint = _track.getNextTrackPoint(pointIndex);
			// Make undo object
			UndoCutAndMove undo = new UndoCutAndMove(_track, startIndex, endIndex, pointIndex);
			// Call track info to move track section
			if (_track.cutAndMoveSection(startIndex, endIndex, pointIndex))
			{
				// Set segment start flags (first track point, next track point, move to point)
				if (firstTrackPoint != null) {firstTrackPoint.setSegmentStart(true);}
				if (nextTrackPoint != null) {nextTrackPoint.setSegmentStart(true);}
				if (moveToTrackPoint != null) {moveToTrackPoint.setSegmentStart(true);}

				// Add undo object to stack, set confirm message
				getUndoStack().add(undo);
				_trackInfo.getSelection().selectRange(-1, -1);
				UpdateMessageBroker.informSubscribers();
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.cutandmove"));
			}
		}
	}

	/**
	 * Select nothing
	 */
	public void selectNone()
	{
		// deselect point, range and photo
		_trackInfo.getSelection().clearAll();
		_track.clearDeletionMarkers();
	}


	/**
	 * Show a map url in an external browser
	 * @param inSourceIndex index of map source to use
	 */
	public void showExternalMap(int inSourceIndex)
	{
		BrowserLauncher.launchBrowser(UrlGenerator.generateUrl(inSourceIndex, _trackInfo));
	}


	@Override
	public void onFileLoaded(FileInfo info) {
		TrackFileInfo trackFile = (TrackFileInfo) info;
		informDataLoaded(trackFile.getFieldArray(), trackFile.getDataArray(), 
				Altitude.Format.METRES, trackFile.getSource(0), 
				trackFile.getTrackNameList(), trackFile.getLinkArray(),
				trackFile.getGpxMetaData());
		super.onFileLoaded(info);
	}


	/**
	 * Receive loaded data and start load
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inAltFormat altitude format
	 * @param inSourceInfo information about the source of the data
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray,
			Altitude.Format inAltFormat, SourceInfo inSourceInfo)
	{
		informDataLoaded(inFieldArray, inDataArray, inAltFormat, inSourceInfo, null, null, null);
	}

	/**
	 * Receive loaded data and determine whether to filter on tracks or not
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inAltFormat altitude format
	 * @param inSourceInfo information about the source of the data
	 * @param inTrackNameList information about the track names
	 * @param inLinkArray array of links to photo/audio files
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray,
			Altitude.Format inAltFormat, SourceInfo inSourceInfo,
			TrackNameList inTrackNameList, String[] inLinkArray, GpxMetadata gpxMetadata)
	{
		// Check whether loaded array can be properly parsed into a Track
		Track loadedTrack = new Track(trackColors[_dataFiles.size() % trackColors.length]);
		loadedTrack.load(inFieldArray, inDataArray, inAltFormat);
		loadedTrack.setGpxMetadata(gpxMetadata);
		if (loadedTrack.getNumPoints() <= 0)
		{
			showErrorMessage("error.load.dialogtitle", "error.load.nopoints");
			// load next file if there's a queue
			loadNextFile();
			return;
		}
		// Check for doubled track
		if (Checker.isDoubledTrack(loadedTrack)) {
			JOptionPane.showMessageDialog(getFrame(), I18nManager.getText("dialog.open.contentsdoubled"),
					I18nManager.getText("function.open"), JOptionPane.WARNING_MESSAGE);
		}
		// Look at TrackNameList, decide whether to filter or not
		if (inTrackNameList != null && inTrackNameList.getNumTracks() > 1)
		{
			// Launch a dialog to let the user choose which tracks to load, then continue
			new SelectTracksFunction(this, inFieldArray, inDataArray, inAltFormat, inSourceInfo,
					inTrackNameList).begin();
		}
		else {
			// go directly to load
			informDataLoaded(loadedTrack, inSourceInfo);
		}
	}


	/**
	 * Receive loaded data and optionally merge with current Track
	 * @param inLoadedTrack loaded track
	 * @param inSourceInfo information about the source of the data
	 */
	public void informDataLoaded(Track inLoadedTrack, SourceInfo inSourceInfo)
	{
		// Decide whether to load or append
		if (_track.getNumPoints() > 0)
		{
			// ask whether to replace or append
			int answer = 0;
			if (getDataFiles() == null || isFirstDataFile()) {
				answer = JOptionPane.showConfirmDialog(getFrame(),
						I18nManager.getText("dialog.openappend.text"),
						I18nManager.getText("dialog.openappend.title"),
						JOptionPane.YES_NO_CANCEL_OPTION);
			}
			else {
				// Automatically append if there's a file load queue
				answer = JOptionPane.YES_OPTION;
			}
			if (answer == JOptionPane.YES_OPTION)
			{
				// append data to current Track
				UndoLoad undo = new UndoLoad(inLoadedTrack, inSourceInfo, _track.getNumPoints(), inLoadedTrack.getNumPoints());
				getUndoStack().add(undo);
				_track.combine(inLoadedTrack);
				// set source information
				inSourceInfo.populatePointObjects(_track, inLoadedTrack.getNumPoints());
				_trackInfo.getFileInfo().addSource(inSourceInfo);

				getCanvas().zoomToFit();
			}
			else if (answer == JOptionPane.NO_OPTION)
			{
				UndoLoad undo = new UndoLoad(inLoadedTrack, inSourceInfo, _trackInfo, inLoadedTrack.getNumPoints(), UndoLoad.REPLACE);
				getUndoStack().add(undo);
				_lastSavePosition = getUndoStack().size();
				_trackInfo.getSelection().clearAll();
				_track.load(inLoadedTrack);
				inSourceInfo.populatePointObjects(_track, _track.getNumPoints());
				_trackInfo.getFileInfo().replaceSource(inSourceInfo);
				getCanvas().zoomToFit();
			}
		}
		else
		{
			// Currently no data held, so transfer received data
			UndoLoad undo = new UndoLoad(inLoadedTrack, inSourceInfo, _trackInfo, inLoadedTrack.getNumPoints(), UndoLoad.LOAD);
			getUndoStack().add(undo);
			_lastSavePosition = getUndoStack().size();
			_trackInfo.getSelection().clearAll();
			_track.load(inLoadedTrack);
			inSourceInfo.populatePointObjects(_track, _track.getNumPoints());
			_trackInfo.getFileInfo().addSource(inSourceInfo);
			getCanvas().zoomToFit();
		}
		UpdateMessageBroker.informSubscribers();
		// Update status bar
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.loadfile")
				+ " '" + inSourceInfo.getName() + "'");
		// update menu
		getMenuManager().informFileLoaded();
		// load next file if there's a queue
		loadNextFile();
	}

	/**
	 * Inform the app that NO data was loaded, eg cancel pressed
	 * Only needed if there's another file waiting in the queue
	 */
	public void informNoDataLoaded()
	{
		// Load next file if there's a queue
		loadNextFile();
	}

	/**
	 * Inform the app that the data has been saved
	 */
	@Override
	public void informDataSaved()
	{
		super.informDataSaved();
		_lastSavePosition = getUndoStack().size();
	}
}
