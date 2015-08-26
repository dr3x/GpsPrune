package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.ColourUtils;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.ColourChooser;
import tim.prune.gui.ColourPatch;
import tim.prune.gui.DialogCloser;
import tim.prune.load.GenericFileFilter;

/**
 * Class to export track information
 * into a specified Kml or Kmz file
 */
public class KmlExporter extends GenericFunction implements Runnable
{
	private TrackInfo _trackInfo = null;
	private Track _track = null;
	private JDialog _dialog = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _altitudesCheckbox = null;
	private JCheckBox _kmzCheckbox = null;
	private ColourPatch _colourPatch = null;
	private JFileChooser _fileChooser = null;
	private File _exportFile = null;
	private JButton _okButton = null;
	private ColourChooser _colourChooser = null;

	// Filename of Kml file within zip archive
	private static final String KML_FILENAME_IN_KMZ = "doc.kml";
	// Default width and height of thumbnail images in Kmz
	private static final int DEFAULT_THUMBNAIL_WIDTH = 240;
	private static final int DEFAULT_THUMBNAIL_HEIGHT = 240;
	// Default track colour
	private static final Color DEFAULT_TRACK_COLOUR = new Color(204, 0, 0); // red


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public KmlExporter(App inApp)
	{
		super(inApp);
		_trackInfo = ((PruneApp)inApp).getTrackInfo();
		_track = _trackInfo.getTrack();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportkml";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window including whether to compress to kmz (and include pictures) or not
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_colourChooser = new ColourChooser(_dialog);
		}
		enableCheckboxes();
		_descriptionField.setEnabled(true);
		_okButton.setEnabled(true);
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a central panel with the text box and checkboxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new FlowLayout());
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportkml.text")));
		_descriptionField = new JTextField(20);
		_descriptionField.addKeyListener(new DialogCloser(_dialog));
		descPanel.add(_descriptionField);
		descPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(descPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// point type selection
		_pointTypeSelector = new PointTypeSelector();
		_pointTypeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_pointTypeSelector);
		// Colour definition
		Color trackColour = ColourUtils.colourFromHex(Config.getConfigString(Config.KEY_KML_TRACK_COLOUR));
		if (trackColour == null) {
			trackColour = DEFAULT_TRACK_COLOUR;
		}
		_colourPatch = new ColourPatch(trackColour);
		_colourPatch.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				_colourChooser.showDialog(_colourPatch.getBackground());
				Color colour = _colourChooser.getChosenColour();
				if (colour != null) _colourPatch.setColour(colour);
			}
		});
		JPanel colourPanel = new JPanel();
		colourPanel.add(new JLabel(I18nManager.getText("dialog.exportkml.trackcolour")));
		colourPanel.add(_colourPatch);
		mainPanel.add(colourPanel);
		// Checkbox for altitude export
		_altitudesCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.altitude"));
		_altitudesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_altitudesCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_altitudesCheckbox);
		// Checkboxes for kmz export and image export
		_kmzCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.kmz"));
		_kmzCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_kmzCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		_kmzCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// enable image checkbox if kmz activated
				enableCheckboxes();
			}
		});
		mainPanel.add(_kmzCheckbox);
		mainPanel.add(Box.createVerticalStrut(10));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExport();
			}
		};
		_okButton.addActionListener(okListener);
		_descriptionField.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Enable the checkboxes according to data
	 */
	private void enableCheckboxes()
	{
		_pointTypeSelector.init(_trackInfo);
		boolean hasAltitudes = _track.hasData(Field.ALTITUDE);
		if (!hasAltitudes) {_altitudesCheckbox.setSelected(false);}
	}


	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, now validate selection checkboxes
		if (!_pointTypeSelector.getAnythingSelected()) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.kmlkmz", new String[] {"kml", "kmz"}));
			// start from directory in config which should be set
			String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
		}
		String requiredExtension = null, otherExtension = null;
		if (_kmzCheckbox.isSelected()) {
			requiredExtension = ".kmz"; otherExtension = ".kml";
		}
		else {
			requiredExtension = ".kml"; otherExtension = ".kmz";
		}
		_fileChooser.setAcceptAllFileFilterUsed(false);
		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = _fileChooser.getSelectedFile();
				if (file.getName().toLowerCase().endsWith(otherExtension))
				{
					String path = file.getAbsolutePath();
					file = new File(path.substring(0, path.length()-otherExtension.length()) + requiredExtension);
				}
				else if (!file.getName().toLowerCase().endsWith(requiredExtension))
				{
					file = new File(file.getAbsolutePath() + requiredExtension);
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// New file or overwrite confirmed, so initiate export in separate thread
					_exportFile = file;
					new Thread(this).start();
				}
				else
				{
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
	}


	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		// Disable ok button to stop second go
		_okButton.setEnabled(false);
		_descriptionField.setEnabled(false);

		// Determine photo thumbnail size from config
		int thumbWidth = Config.getConfigInt(Config.KEY_KMZ_IMAGE_WIDTH);
		if (thumbWidth < DEFAULT_THUMBNAIL_WIDTH) {thumbWidth = DEFAULT_THUMBNAIL_WIDTH;}
		int thumbHeight = Config.getConfigInt(Config.KEY_KMZ_IMAGE_HEIGHT);
		if (thumbHeight < DEFAULT_THUMBNAIL_HEIGHT) {thumbHeight = DEFAULT_THUMBNAIL_HEIGHT;}

		OutputStreamWriter writer = null;
		ZipOutputStream zipOutputStream = null;
		try
		{
			// Select writer according to whether kmz requested or not
			if (!_kmzCheckbox.isSelected())
			{
				// normal writing to file
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			}
			else
			{
				// kmz requested - need zip output stream
				zipOutputStream = new ZipOutputStream(new FileOutputStream(_exportFile));
				writer = new OutputStreamWriter(zipOutputStream);
				// Make an entry in the zip file for the kml file
				ZipEntry kmlEntry = new ZipEntry(KML_FILENAME_IN_KMZ);
				zipOutputStream.putNextEntry(kmlEntry);
			}
			// write file
			final int numPoints = exportData(writer);
			// update config with selected track colour
			Config.setConfigString(Config.KEY_KML_TRACK_COLOUR, ColourUtils.makeHexCode(_colourPatch.getBackground()));

			// close zip entry if necessary
			if (zipOutputStream != null)
			{
				// Make sure all buffered data in writer is flushed
				writer.flush();
				// Close off this entry in the zip file
				zipOutputStream.closeEntry();
			}

			// close file
			writer.close();
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// show confirmation
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + _exportFile.getAbsolutePath());
			// export successful so need to close dialog and return
			_dialog.dispose();
			return;
		}
		catch (IOException ioe)
		{
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
	 * @param inExportImages true if image thumbnails are to be referenced
	 * @return number of points written
	 */
	private int exportData(OutputStreamWriter inWriter)
	throws IOException
	{
		boolean writeTrack = _pointTypeSelector.getTrackpointsSelected();
		boolean writeWaypoints = _pointTypeSelector.getWaypointsSelected();
		boolean justSelection = _pointTypeSelector.getJustSelection();
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.1\">\n<Folder>\n");
		inWriter.write("\t<name>");
		if (_descriptionField != null && _descriptionField.getText() != null && !_descriptionField.getText().equals(""))
		{
			inWriter.write(_descriptionField.getText());
		}
		else {
			inWriter.write("Export from Prune");
		}
		inWriter.write("</name>\n");

		// Examine selection if required
		int selStart = -1, selEnd = -1;
		if (justSelection) {
			selStart = _trackInfo.getSelection().getStart();
			selEnd = _trackInfo.getSelection().getEnd();
		}

		boolean absoluteAltitudes = _altitudesCheckbox.isSelected();
		int i = 0;
		DataPoint point = null;
		boolean hasTrackpoints = false;
		final int numPoints = _track.getNumPoints();
		int numSaved = 0;
		// Loop over waypoints
		for (i=0; i<numPoints; i++)
		{
			point = _track.getPoint(i);
			boolean writeCurrentPoint = !justSelection || (i>=selStart && i<=selEnd);
			// Make a blob for each waypoint
			if (point.isWaypoint())
			{
				if (writeWaypoints && writeCurrentPoint)
				{
					exportWaypoint(point, inWriter, absoluteAltitudes);
					numSaved++;
				}
			}
			else
			{
				hasTrackpoints = true;
			}
		}
		// Make a line for the track, if there is one
		if (hasTrackpoints && writeTrack)
		{
			// Set up strings for start and end of track segment
			String trackStart = "\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
				+ "\t\t\t\t<color>cc" + reverse(ColourUtils.makeHexCode(_colourPatch.getBackground())) + "</color>\n"
				+ "\t\t\t\t<width>4</width>\n\t\t\t</LineStyle>\n"
				+ "\t\t\t<PolyStyle><color>33cc0000</color></PolyStyle>\n"
				+ "\t\t</Style>\n\t\t<LineString>\n";
			if (absoluteAltitudes) {
				trackStart += "\t\t\t<extrude>1</extrude>\n\t\t\t<altitudeMode>absolute</altitudeMode>\n";
			}
			else {
				trackStart += "\t\t\t<altitudeMode>clampToGround</altitudeMode>\n";
			}
			trackStart += "\t\t\t<coordinates>";
			String trackEnd = "\t\t\t</coordinates>\n\t\t</LineString>\n\t</Placemark>";

			// Start segment
			inWriter.write(trackStart);
			// Loop over track points
			boolean firstTrackpoint = true;
			for (i=0; i<numPoints; i++)
			{
				point = _track.getPoint(i);
				boolean writeCurrentPoint = !justSelection || (i>=selStart && i<=selEnd);
				if (!point.isWaypoint() && writeCurrentPoint)
				{
					// start new track segment if necessary
					if (point.getSegmentStart() && !firstTrackpoint) {
						inWriter.write(trackEnd);
						inWriter.write(trackStart);
					}
					exportTrackpoint(point, inWriter);
					numSaved++;
					firstTrackpoint = false;
				}
			}
			// end segment
			inWriter.write(trackEnd);
		}
		inWriter.write("</Folder>\n</kml>");
		return numSaved;
	}

	/**
	 * Reverse the hex code for the colours for KML's stupid backwards format
	 * @param inCode colour code rrggbb
	 * @return kml code bbggrr
	 */
	private static String reverse(String inCode)
	{
		return inCode.substring(4, 6) + inCode.substring(2, 4) + inCode.substring(0, 2);
	}

	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @param inAbsoluteAltitude true for absolute altitude
	 * @throws IOException on write failure
	 */
	private void exportWaypoint(DataPoint inPoint, Writer inWriter, boolean inAbsoluteAltitude) throws IOException
	{
		String name = inPoint.getWaypointName().trim();
		exportNamedPoint(inPoint, inWriter, name, null, null, inAbsoluteAltitude);
	}

	/**
	 * Export the specified named point into the file, like waypoint or photo point
	 * @param inPoint data point
	 * @param inWriter writer object
	 * @param inName name of point
	 * @param inDesc description of point, or null
	 * @param inStyle style of point, or null
	 * @param inAbsoluteAltitude true for absolute altitudes
	 * @throws IOException on write failure
	 */
	private void exportNamedPoint(DataPoint inPoint, Writer inWriter, String inName,
		String inDesc, String inStyle, boolean inAbsoluteAltitude)
	throws IOException
	{
		inWriter.write("\t<Placemark>\n\t\t<name>");
		inWriter.write(inName);
		inWriter.write("</name>\n");
		if (inDesc != null)
		{
			// Write out description
			inWriter.write("<description>");
			inWriter.write(inDesc);
			inWriter.write("</description>");
		}
		if (inStyle != null)
		{
			inWriter.write("<styleUrl>#");
			inWriter.write(inStyle);
			inWriter.write("</styleUrl>\n");
		}
		inWriter.write("\t\t<Point>\n");
		if (inAbsoluteAltitude && inPoint.hasAltitude()) {
			inWriter.write("\t\t\t<altitudeMode>absolute</altitudeMode>\n");
		}
		else {
			inWriter.write("\t\t\t<altitudeMode>clampToGround</altitudeMode>\n");
		}
		inWriter.write("\t\t\t<coordinates>");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		// Altitude if point has one
		if (inPoint.hasAltitude()) {
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
		}
		else {
			inWriter.write('0');
		}
		inWriter.write("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		// Altitude if point has one
		inWriter.write(',');
		if (inPoint.hasAltitude()) {
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
		}
		else {
			inWriter.write('0');
		}
		inWriter.write('\n');
	}
}
