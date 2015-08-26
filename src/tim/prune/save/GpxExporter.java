package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.TrackInfo;
import tim.prune.gui.DialogCloser;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.xml.GpxCacherList;


/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter extends GenericFunction implements Runnable
{
	private TrackInfo _trackInfo = null;
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _timestampsCheckbox = null;
	private JCheckBox _copySourceCheckbox = null;
	private File _exportFile = null;

	/** this program name */
	private static final String GPX_CREATOR = "Prune";


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GpxExporter(App inApp)
	{
		super(inApp);
		_trackInfo = ((PruneApp)inApp).getTrackInfo();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportgpx";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_pointTypeSelector.init(_pruneApp.getTrackInfo());
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a central panel with the text boxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(2, 2));
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.name")));
		_nameField = new JTextField(10);
		descPanel.add(_nameField);
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.desc")));
		_descriptionField = new JTextField(10);
		descPanel.add(_descriptionField);
		mainPanel.add(descPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		// point type selection (track points, waypoints, photo points)
		_pointTypeSelector = new PointTypeSelector();
		mainPanel.add(_pointTypeSelector);
		// checkboxes for timestamps and copying
		JPanel checkPanel = new JPanel();
		_timestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.includetimestamps"));
		_timestampsCheckbox.setSelected(true);
		checkPanel.add(_timestampsCheckbox);
		_copySourceCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.copysource"));
		_copySourceCheckbox.setSelected(true);
		checkPanel.add(_copySourceCheckbox);
		mainPanel.add(checkPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// close dialog if escape pressed
		_nameField.addKeyListener(new DialogCloser(_dialog));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExport();
			}
		};
		okButton.addActionListener(okListener);
		_descriptionField.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, so check selections
		if (!_pointTypeSelector.getAnythingSelected()) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		File saveFile = chooseGpxFile(_parentFrame);
		if (saveFile != null)
		{
			// New file or overwrite confirmed, so initiate export in separate thread
			_exportFile = saveFile;
			new Thread(this).start();
		}
	}

	/**
	 * Select a GPX file to save to
	 * @param inParentFrame parent frame for file chooser dialog
	 * @return selected File, or null if selection cancelled
	 */
	public static File chooseGpxFile(JFrame inParentFrame)
	{
		File saveFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
		fileChooser.setAcceptAllFileFilterUsed(false);
		// start from directory in config which should be set
		String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
		if (configDir != null) {fileChooser.setCurrentDirectory(new File(configDir));}

		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (fileChooser.showSaveDialog(inParentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = fileChooser.getSelectedFile();
				// Check file extension
				if (!file.getName().toLowerCase().endsWith(".gpx"))
				{
					file = new File(file.getAbsolutePath() + ".gpx");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(inParentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// new file or overwrite confirmed
					saveFile = file;
				}
				else
				{
					// file exists and overwrite cancelled - select again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
		return saveFile;
	}

	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		OutputStreamWriter writer = null;
		try
		{
			// normal writing to file
			writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			final boolean[] saveFlags = {_pointTypeSelector.getTrackpointsSelected(), _pointTypeSelector.getWaypointsSelected(),
				_pointTypeSelector.getJustSelection(), _timestampsCheckbox.isSelected()};
			// write file
			final int numPoints = exportData(writer, _trackInfo, _nameField.getText(),
				_descriptionField.getText(), saveFlags, _copySourceCheckbox.isSelected());

			// close file
			writer.close();
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// Show confirmation
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + _exportFile.getAbsolutePath());
			// export successful so need to close dialog and return
			_dialog.dispose();
			return;
		}
		catch (IOException ioe)
		{
			// System.out.println("Exception: " + ioe.getClass().getName() + " - " + ioe.getMessage());
			try {
				if (writer != null) writer.close();
			}
			catch (IOException ioe2) {}
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("error.save.failed") + " : " + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		// if not returned already, export failed so need to recall the file selection
		startExport();
	}


	/**
	 * Export the information to the given writer
	 * @param inWriter writer object
	 * @param inInfo track info object
	 * @param inName name of track (optional)
	 * @param inDesc description of track (optional)
	 * @param inSaveFlags array of booleans to export tracks, waypoints, selection, timestamps
	 * @param inUseCopy true to copy source if available
	 * @return number of points written
	 * @throws IOException if io errors occur on write
	 */
	public static int exportData(OutputStreamWriter inWriter, TrackInfo inInfo, String inName,
		String inDesc, boolean[] inSaveFlags, boolean inUseCopy) throws IOException
	{
		// Instantiate source file cachers in case we want to copy output
		GpxCacherList gpxCachers = null;
		if (inUseCopy) gpxCachers = new GpxCacherList(inInfo.getFileInfo());
		// Write or copy headers
		inWriter.write(getXmlHeaderString(inWriter));
		inWriter.write(getGpxHeaderString(gpxCachers));
		// Name field
		String trackName = "PruneTrack";
		if (inName != null && !inName.equals(""))
		{
			trackName = inName;
			inWriter.write("\t<name>");
			inWriter.write(trackName);
			inWriter.write("</name>\n");
		}
		// Description field
		inWriter.write("\t<desc>");
		inWriter.write((inDesc != null && !inDesc.equals(""))?inDesc:"Export from Prune");
		inWriter.write("</desc>\n");

		int i = 0;
		DataPoint point = null;
		final boolean exportTrackpoints = inSaveFlags[0];
		final boolean exportWaypoints = inSaveFlags[1];
		final boolean exportSelection = inSaveFlags[2];
		final boolean exportTimestamps = inSaveFlags[3];
		// Examine selection
		int selStart = -1, selEnd = -1;
		if (exportSelection) {
			selStart = inInfo.getSelection().getStart();
			selEnd = inInfo.getSelection().getEnd();
		}
		// Loop over waypoints
		final int numPoints = inInfo.getTrack().getNumPoints();
		int numSaved = 0;
		for (i=0; i<numPoints; i++)
		{
			point = inInfo.getTrack().getPoint(i);
			if (!exportSelection || (i>=selStart && i<=selEnd)) {
				// Make a wpt element for each waypoint
				if (point.isWaypoint()) {
					if (exportWaypoints)
					{
						String pointSource = (inUseCopy?getPointSource(gpxCachers, point):null);
						if (pointSource != null) {
							inWriter.write(pointSource);
							inWriter.write('\n');
						}
						else {
							exportWaypoint(point, inWriter, exportTimestamps);
						}
						numSaved++;
					}
				}
			}
		}
		// Export both route points and then track points
		if (exportTrackpoints)
		{
			// Output all route points (if any)
			numSaved += writeTrackPoints(inWriter, inInfo, exportSelection, exportTrackpoints, 
					exportTimestamps, true, gpxCachers, "<rtept", "\t<rte><number>1</number>\n",
				null, "\t</rte>\n");
			// Output all track points, if any
			String trackStart = "\t<trk><name>" + trackName + "</name><number>1</number><trkseg>\n";
			numSaved += writeTrackPoints(inWriter, inInfo, exportSelection, exportTrackpoints, 
					exportTimestamps, false, gpxCachers, "<trkpt", trackStart,
				"\t</trkseg>\n\t<trkseg>\n", "\t</trkseg></trk>\n");
		}

		inWriter.write("</gpx>\n");
		return numSaved;
	}

	/**
	 * Loop through the track outputting the relevant track points
	 * @param inWriter writer object for output
	 * @param inInfo track info object containing track
	 * @param inExportSelection true to just output current selection
	 * @param inExportTrackpoints true to output track points
	 * @param inExportTimestamps true to include timestamps in export
	 * @param inOnlyCopies true to only export if source can be copied
	 * @param inCachers list of GpxCachers
	 * @param inPointTag tag to match for each point
	 * @param inStartTag start tag to output
	 * @param inSegmentTag tag to output between segments (or null)
	 * @param inEndTag end tag to output
	 */
	private static int writeTrackPoints(OutputStreamWriter inWriter,
		TrackInfo inInfo, boolean inExportSelection, boolean inExportTrackpoints,
		boolean exportTimestamps,
		boolean inOnlyCopies, GpxCacherList inCachers, String inPointTag,
		String inStartTag, String inSegmentTag, String inEndTag)
	throws IOException
	{
		// Note: far too many input parameters to this method but avoids duplication
		// of output functionality for writing track points and route points
		int numPoints = inInfo.getTrack().getNumPoints();
		int selStart = inInfo.getSelection().getStart();
		int selEnd = inInfo.getSelection().getEnd();
		int numSaved = 0;
		// Loop over track points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inInfo.getTrack().getPoint(i);
			if ((!inExportSelection || (i>=selStart && i<=selEnd)) && !point.isWaypoint())
			{
				if (inExportTrackpoints)
				{
					// get the source from the point (if any)
					String pointSource = getPointSource(inCachers, point);
					// Clear point source if it's the wrong type of point (eg changed from waypoint or route point)
					if (pointSource != null && !pointSource.toLowerCase().startsWith(inPointTag)) {pointSource = null;}
					if (pointSource != null || !inOnlyCopies)
					{
						// restart track segment if necessary
						if ((numSaved > 0) && point.getSegmentStart() && (inSegmentTag != null)) {
							inWriter.write(inSegmentTag);
						}
						if (numSaved == 0) {inWriter.write(inStartTag);}
						if (pointSource != null) {
							inWriter.write(pointSource);
							inWriter.write('\n');
						}
						else {
							if (!inOnlyCopies) {exportTrackpoint(point, inWriter, exportTimestamps);}
						}
						numSaved++;
					}
				}
			}
		}
		if (numSaved > 0) {inWriter.write(inEndTag);}
		return numSaved;
	}


	/**
	 * Get the point source for the specified point
	 * @param inCachers list of GPX cachers to ask for source
	 * @param inPoint point object
	 * @return xml source if available, or null otherwise
	 */
	private static String getPointSource(GpxCacherList inCachers, DataPoint inPoint)
	{
		if (inCachers == null || inPoint == null) {return null;}
		String source = inCachers.getSourceString(inPoint);
		if (source == null || !inPoint.isModified()) {return source;}
		// Point has been modified - maybe it's possible to modify the source
		source = replaceGpxTags(source, "lat=\"", "\"", inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "lon=\"", "\"", inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "<ele>", "</ele>", inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
		source = replaceGpxTags(source, "<time>", "</time>", inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
		if (inPoint.isWaypoint()) {source = replaceGpxTags(source, "<name>", "</name>", inPoint.getWaypointName());}  // only for waypoints
		return source;
	}

	/**
	 * Replace the given value into the given XML string
	 * @param inSource source XML for point
	 * @param inStartTag start tag for field
	 * @param inEndTag end tag for field
	 * @param inValue value to replace between start tag and end tag
	 * @return modified String, or null if not possible
	 */
	private static String replaceGpxTags(String inSource, String inStartTag, String inEndTag, String inValue)
	{
		if (inSource == null) {return null;}
		// Look for start and end tags within source
		final int startPos = inSource.indexOf(inStartTag);
		final int endPos = inSource.indexOf(inEndTag, startPos+inStartTag.length());
		if (startPos > 0 && endPos > 0)
		{
			String origValue = inSource.substring(startPos + inStartTag.length(), endPos);
			if (inValue != null && origValue.equals(inValue)) {
				// Value unchanged
				return inSource;
			}
			else if (inValue == null || inValue.equals("")) {
				// Need to delete value
				return inSource.substring(0, startPos) + inSource.substring(endPos + inEndTag.length());
			}
			else {
				// Need to replace value
				return inSource.substring(0, startPos+inStartTag.length()) + inValue + inSource.substring(endPos);
			}
		}
		// Value not found for this field in original source
		if (inValue == null || inValue.equals("")) {return inSource;}
		return null;
	}

	/**
	 * Get the header string for the xml document including encoding
	 * @param inWriter writer object
	 * @return header string defining encoding
	 */
	private static String getXmlHeaderString(OutputStreamWriter inWriter)
	{
		String encoding = inWriter.getEncoding();
		try {
			encoding =  Charset.forName(encoding).name();
		}
		catch (Exception e) {} // ignore failure to find encoding
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n";
	}

	/**
	 * Get the header string for the gpx tag
	 * @param inCachers cacher list to ask for headers, if available
	 * @return header string from cachers or as default
	 */
	private static String getGpxHeaderString(GpxCacherList inCachers)
	{
		String gpxHeader = null;
		if (inCachers != null) {gpxHeader = inCachers.getFirstHeader();}
		if (gpxHeader == null || gpxHeader.length() < 5)
		{
			// Create default (1.0) header
			gpxHeader = "<gpx version=\"1.0\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
				+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
		}
		return gpxHeader + "\n";
	}

	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @param inTimestamps true to export timestamps too
	 * @param inPhoto true to export link to photo
	 * @param inAudio true to export link to audio
	 * @throws IOException on write failure
	 */
	private static void exportWaypoint(DataPoint inPoint, Writer inWriter, boolean inTimestamps)
		throws IOException
	{
		inWriter.write("\t<wpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude if available
		if (inPoint.hasAltitude())
		{
			inWriter.write("\t\t<ele>");
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
			inWriter.write("</ele>\n");
		}
		// timestamp if available (point might have timestamp and then be turned into a waypoint)
		if (inPoint.hasTimestamp() && inTimestamps)
		{
			inWriter.write("\t\t<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
			inWriter.write("</time>\n");
		}
		// write waypoint name after elevation and time
		inWriter.write("\t\t<name>");
		inWriter.write(inPoint.getWaypointName().trim());
		inWriter.write("</name>\n");
		// write waypoint type if any
		String type = inPoint.getFieldValue(Field.WAYPT_TYPE);
		if (type != null)
		{
			type = type.trim();
			if (!type.equals(""))
			{
				inWriter.write("\t\t<type>");
				inWriter.write(type);
				inWriter.write("</type>\n");
			}
		}
		inWriter.write("\t</wpt>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 * @param inTimestamps true to export timestamps too
	 * @param inExportPhoto true to export photo link
	 * @param inExportAudio true to export audio link
	 */
	private static void exportTrackpoint(DataPoint inPoint, Writer inWriter, boolean inTimestamps)
		throws IOException
	{
		inWriter.write("\t\t<trkpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">");
		// altitude
		if (inPoint.hasAltitude())
		{
			inWriter.write("<ele>");
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
			inWriter.write("</ele>");
		}
		// timestamp if available (and selected)
		if (inPoint.hasTimestamp() && inTimestamps)
		{
			inWriter.write("<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
			inWriter.write("</time>");
		}
		inWriter.write("</trkpt>\n");
	}
}
