package tim.prune.function.charts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.Distance.Units;
import tim.prune.load.GenericFileFilter;

/**
 * Class to manage the generation of charts using gnuplot
 */
public class Charter extends GenericFunction
{
	/** dialog object, cached */
	private JDialog _dialog = null;
	/** radio button for distance axis */
	private JRadioButton _distanceRadio = null;
	/** radio button for time axis */
	private JRadioButton _timeRadio = null;
	/** array of checkboxes for specifying y axes */
	private JCheckBox[] _yAxesBoxes = null;
	/** radio button for svg output */
	private JRadioButton _svgRadio = null;
	/** file chooser for saving svg file */
	private JFileChooser _fileChooser = null;
	/** text field for svg width */
	private JTextField _svgWidthField = null;
	/** text field for svg height */
	private JTextField _svgHeightField = null;

	/** Default dimensions of Svg file */
	private static final String DEFAULT_SVG_WIDTH  = "800";
	private static final String DEFAULT_SVG_HEIGHT = "400";


	/**
	 * Constructor from superclass
	 * @param inApp app object
	 */
	public Charter(App inApp)
	{
		super(inApp);
	}

	/**
	 * @return key for function name
	 */
	public String getNameKey()
	{
		return "function.charts";
	}

	/**
	 * Show the dialog
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
		if (setupDialog(_app.getTrackInfo().getTrack())) {
			_dialog.setVisible(true);
		}
		else {
			_app.showErrorMessage(getNameKey(), "dialog.charts.needaltitudeortimes");
		}
	}


	/**
	 * Make the dialog components
	 * @return panel containing gui elements
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// x axis choice
		JPanel axisPanel = new JPanel();
		axisPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.charts.xaxis")));
		_distanceRadio = new JRadioButton(I18nManager.getText("fieldname.distance"));
		_distanceRadio.setSelected(true);
		_timeRadio = new JRadioButton(I18nManager.getText("fieldname.time"));
		ButtonGroup axisGroup = new ButtonGroup();
		axisGroup.add(_distanceRadio); axisGroup.add(_timeRadio);
		axisPanel.add(_distanceRadio); axisPanel.add(_timeRadio);
		mainPanel.add(axisPanel);

		// y axis choices
		JPanel yPanel = new JPanel();
		yPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.charts.yaxis")));
		_yAxesBoxes = new JCheckBox[4]; // dist altitude speed vertspeed (time not available on y axis)
		_yAxesBoxes[0] = new JCheckBox(I18nManager.getText("fieldname.distance"));
		_yAxesBoxes[1] = new JCheckBox(I18nManager.getText("fieldname.altitude"));
		_yAxesBoxes[1].setSelected(true);
		_yAxesBoxes[2] = new JCheckBox(I18nManager.getText("fieldname.speed"));
		_yAxesBoxes[3] = new JCheckBox(I18nManager.getText("fieldname.verticalspeed"));
		for (int i=0; i<4; i++) {
			yPanel.add(_yAxesBoxes[i]);
		}
		mainPanel.add(yPanel);

		// Add validation to prevent choosing invalid (ie dist/dist) combinations
		ActionListener xAxisListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableYbox(0, _timeRadio.isSelected());
			}
		};
		_timeRadio.addActionListener(xAxisListener);
		_distanceRadio.addActionListener(xAxisListener);

		// output buttons
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.charts.output")));
		outputPanel.setLayout(new BorderLayout());
		JPanel radiosPanel = new JPanel();
		JRadioButton screenRadio = new JRadioButton(I18nManager.getText("dialog.charts.screen"));
		screenRadio.setSelected(true);
		_svgRadio = new JRadioButton(I18nManager.getText("dialog.charts.svg"));
		ButtonGroup outputGroup = new ButtonGroup();
		outputGroup.add(screenRadio); outputGroup.add(_svgRadio);
		radiosPanel.add(screenRadio); radiosPanel.add(_svgRadio);
		outputPanel.add(radiosPanel, BorderLayout.NORTH);
		// panel for svg width, height
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new GridLayout(2, 2, 10, 1));
		JLabel widthLabel = new JLabel(I18nManager.getText("dialog.charts.svgwidth"));
		widthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		sizePanel.add(widthLabel);
		_svgWidthField = new JTextField(DEFAULT_SVG_WIDTH, 5);
		sizePanel.add(_svgWidthField);
		JLabel heightLabel = new JLabel(I18nManager.getText("dialog.charts.svgheight"));
		heightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		sizePanel.add(heightLabel);
		_svgHeightField = new JTextField(DEFAULT_SVG_HEIGHT, 5);
		sizePanel.add(_svgHeightField);

		outputPanel.add(sizePanel, BorderLayout.EAST);
		mainPanel.add(outputPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel on bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// Gnuplot button
		JButton gnuplotButton = new JButton(I18nManager.getText("button.gnuplotpath"));
		gnuplotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setGnuplotPath();
			}
		});
		buttonPanel.add(gnuplotButton);
		// Cancel button
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);
		// ok button
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showChart(_app.getTrackInfo().getTrack());
				_dialog.setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Set up the dialog according to the track contents
	 * @param inTrack track object
	 * @return true if it's all ok
	 */
	private boolean setupDialog(Track inTrack)
	{
		boolean hasTimes = inTrack.hasData(Field.TIMESTAMP);
		boolean hasAltitudes = inTrack.getAltitudeRange().hasRange();
		_timeRadio.setEnabled(hasTimes);

		// Add checks to prevent choosing unavailable combinations
		if (!hasTimes) {
			_distanceRadio.setSelected(true);
		}
		enableYbox(0, !_distanceRadio.isSelected());
		enableYbox(1, hasAltitudes);
		enableYbox(2, hasTimes);
		enableYbox(3, hasTimes && hasAltitudes);
		return (hasTimes || hasAltitudes);
	}


	/**
	 * Enable or disable the given y axis checkbox
	 * @param inIndex index of checkbox
	 * @param inFlag true to enable
	 */
	private void enableYbox(int inIndex, boolean inFlag)
	{
		_yAxesBoxes[inIndex].setEnabled(inFlag);
		if (!inFlag) {
			_yAxesBoxes[inIndex].setSelected(inFlag);
		}
	}

	/**
	 * Show the chart for the specified track
	 * @param inTrack track object containing data
	 */
	private void showChart(Track inTrack)
	{
		int numCharts = 0;
		for (int i=0; i<_yAxesBoxes.length; i++) {
			if (_yAxesBoxes[i].isSelected()) {
				numCharts++;
			}
		}
		// Select default chart if none selected
		if (numCharts == 0) {
			_yAxesBoxes[1].setSelected(true);
			numCharts = 1;
		}
		int[] heights = getHeights(numCharts);

		boolean showSvg = _svgRadio.isSelected();
		File svgFile = null;
		if (showSvg) {
			svgFile = selectSvgFile();
			if (svgFile == null) {showSvg = false;}
		}
		OutputStreamWriter writer = null;
		try
		{
			Process process = Runtime.getRuntime().exec(Config.getGnuplotPath() + " -persist");
			writer = new OutputStreamWriter(process.getOutputStream());
			if (showSvg)
			{
				writer.write("set terminal svg size " + getSvgValue(_svgWidthField, DEFAULT_SVG_WIDTH) + " "
					+ getSvgValue(_svgHeightField, DEFAULT_SVG_HEIGHT) + "\n");
				writer.write("set out '" + svgFile.getAbsolutePath() + "'\n");
			}
			if (numCharts > 1) {
				writer.write("set multiplot layout " + numCharts + ",1\n");
			}
			// Loop over possible charts
			int chartNum = 0;
			for (int c=0; c<_yAxesBoxes.length; c++)
			{
				if (_yAxesBoxes[c].isSelected())
				{
					writer.write("set size 1," + (0.01*heights[chartNum*2+1]) + "\n");
					writer.write("set origin 0," + (0.01*heights[chartNum*2]) + "\n");
					writeChart(writer, inTrack, _distanceRadio.isSelected(), c);
					chartNum++;
				}
			}
			// Close multiplot if open
			if (numCharts > 1) {
				writer.write("unset multiplot\n");
			}
		}
		catch (Exception e) {
			_app.showErrorMessageNoLookup(getNameKey(), e.getMessage());
		}
		finally {
			try {
				// Close writer
				if (writer != null) writer.close();
			}
			catch (Exception e) {} // ignore
		}
	}


	/**
	 * Parse the given text field's value and return as string
	 * @param inField text field to read from
	 * @param inDefault default value if not valid
	 * @return value of svg dimension as string
	 */
	private static String getSvgValue(JTextField inField, String inDefault)
	{
		int value = 0;
		try {
			value = Integer.parseInt(inField.getText());
		}
		catch (Exception e) {} // ignore, value stays zero
		if (value > 0) {
			return "" + value;
		}
		return inDefault;
	}


	/**
	 * Write out the selected chart to the given Writer object
	 * @param inWriter writer object
	 * @param inTrack Track containing data
	 * @param inDistance true if x axis is distance
	 * @param inYaxis index of y axis
	 * @throws IOException if writing error occurred
	 */
	private static void writeChart(OutputStreamWriter inWriter, Track inTrack, boolean inDistance, int inYaxis)
	throws IOException
	{
		ChartSeries xValues = null, yValues = null;
		ChartSeries distValues = getDistanceValues(inTrack);
		// Choose x values according to axis
		if (inDistance) {
			xValues = distValues;
		}
		else {
			xValues = getTimeValues(inTrack);
		}
		// Choose y values according to axis
		switch (inYaxis)
		{
		case 0: // y axis is distance
			yValues = distValues;
			break;
		case 1: // y axis is altitude
			yValues = getAltitudeValues(inTrack);
			break;
		case 2: // y axis is speed
			yValues = getSpeedValues(inTrack);
			break;
		case 3: // y axis is vertical speed
			yValues = getVertSpeedValues(inTrack);
			break;
		}
		// Make a temporary data file for the output (one per subchart)
		File tempFile = File.createTempFile("prunedata", null);
		tempFile.deleteOnExit();
		// write out values for x and y to temporary file
		FileWriter tempFileWriter = null;
		try {
			tempFileWriter = new FileWriter(tempFile);
			tempFileWriter.write("# Temporary data file for Prune charts\n\n");
			for (int i=0; i<inTrack.getNumPoints(); i++) {
				if (xValues.hasData(i) && yValues.hasData(i)) {
					tempFileWriter.write("" + xValues.getData(i) + ", " + yValues.getData(i) + "\n");
				}
			}
		}
		catch (IOException ioe) { // rethrow
			throw ioe;
		}
		finally {
			try {
				tempFileWriter.close();
			}
			catch (Exception e) {}
		}

		// Set x axis label
		if (inDistance) {
			inWriter.write("set xlabel '" + I18nManager.getText("fieldname.distance") + " (" + getUnitsLabel("units.kilometres.short", "units.miles.short") + ")'\n");
		}
		else {
			inWriter.write("set xlabel '" + I18nManager.getText("fieldname.time") + " (" + I18nManager.getText("units.hours") + ")'\n");
		}

		// set other labels and plot chart
		String chartTitle = null;
		switch (inYaxis)
		{
		case 0: // y axis is distance
			inWriter.write("set ylabel '" + I18nManager.getText("fieldname.distance") + " (" + getUnitsLabel("units.kilometres.short", "units.miles.short") + ")'\n");
			chartTitle = I18nManager.getText("fieldname.distance");
			break;
		case 1: // y axis is altitude
			inWriter.write("set ylabel '" + I18nManager.getText("fieldname.altitude") + " (" + getUnitsLabel("units.metres.short", "units.feet.short") + ")'\n");
			chartTitle = I18nManager.getText("fieldname.altitude");
			break;
		case 2: // y axis is speed
			inWriter.write("set ylabel '" + I18nManager.getText("fieldname.speed") + " (" + getUnitsLabel("units.kmh", "units.mph") + ")'\n");
			chartTitle = I18nManager.getText("fieldname.speed");
			break;
		case 3: // y axis is vertical speed
			inWriter.write("set ylabel '" + I18nManager.getText("fieldname.verticalspeed") + " (" + getUnitsLabel("units.metrespersec", "units.feetpersec") + ")'\n");
			chartTitle = I18nManager.getText("fieldname.verticalspeed");
			break;
		}
		inWriter.write("set style fill solid 0.5 border -1\n");
		inWriter.write("plot '" + tempFile.getAbsolutePath() + "' title '" + chartTitle + "' with filledcurve y1=0 lt rgb \"#009000\"\n");
	}

	/**
	 * Get the units label for the given keys
	 * @param inMetric key if metric
	 * @param inImperial key if imperial
	 * @return display label with appropriate text
	 */
	private static String getUnitsLabel(String inMetric, String inImperial)
	{
		String key = Config.getUseMetricUnits()?inMetric:inImperial;
		return I18nManager.getText(key);
	}


	/**
	 * Calculate the distance values for each point in the given track
	 * @param inTrack track object
	 * @return distance values in a ChartSeries object
	 */
	private static ChartSeries getDistanceValues(Track inTrack)
	{
		// Calculate distances and fill in in values array
		ChartSeries values = new ChartSeries(inTrack.getNumPoints());
		double totalRads = 0;
		DataPoint prevPoint = null, currPoint = null;
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			currPoint = inTrack.getPoint(i);
			if (prevPoint != null && !currPoint.isWaypoint() && !currPoint.getSegmentStart())
			{
				totalRads += DataPoint.calculateRadiansBetween(prevPoint, currPoint);
			}
			if (Config.getUseMetricUnits()) {
				values.setData(i, Distance.convertRadiansToDistance(totalRads, Units.KILOMETRES));
			} else {
				values.setData(i, Distance.convertRadiansToDistance(totalRads, Units.MILES));
			}
			prevPoint = currPoint;
		}
		return values;
	}

	/**
	 * Calculate the time values for each point in the given track
	 * @param inTrack track object
	 * @return time values in a ChartSeries object
	 */
	private static ChartSeries getTimeValues(Track inTrack)
	{
		// Calculate times and fill in in values array
		ChartSeries values = new ChartSeries(inTrack.getNumPoints());
		double seconds = 0.0;
		Timestamp prevTimestamp = null;
		DataPoint currPoint = null;
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			currPoint = inTrack.getPoint(i);
			if (currPoint.hasTimestamp())
			{
				if (!currPoint.getSegmentStart() && prevTimestamp != null) {
					seconds += (currPoint.getTimestamp().getSecondsSince(prevTimestamp));
				}
				values.setData(i, seconds / 60.0 / 60.0);
				prevTimestamp = currPoint.getTimestamp();
			}
		}
		return values;
	}

	/**
	 * Calculate the altitude values for each point in the given track
	 * @param inTrack track object
	 * @return altitude values in a ChartSeries object
	 */
	private static ChartSeries getAltitudeValues(Track inTrack)
	{
		ChartSeries values = new ChartSeries(inTrack.getNumPoints());
		Altitude.Format altFormat = Config.getUseMetricUnits()?Altitude.Format.METRES:Altitude.Format.FEET;
		for (int i=0; i<inTrack.getNumPoints(); i++) {
			if (inTrack.getPoint(i).hasAltitude()) {
				values.setData(i, inTrack.getPoint(i).getAltitude().getValue(altFormat));
			}
		}
		return values;
	}

	/**
	 * Calculate the speed values for each point in the given track
	 * @param inTrack track object
	 * @return speed values in a ChartSeries object
	 */
	private static ChartSeries getSpeedValues(Track inTrack)
	{
		// Calculate speeds and fill in in values array
		ChartSeries values = new ChartSeries(inTrack.getNumPoints());
		DataPoint prevPoint = null, currPoint = null, nextPoint = null;
		DataPoint[] points = getDataPoints(inTrack, false);
		// Loop over collected points
		for (int i=1; i<(points.length-1); i++)
		{
			prevPoint = points[i-1];
			currPoint = points[i];
			nextPoint = points[i+1];
			if (prevPoint != null && currPoint != null && nextPoint != null
				&& nextPoint.getTimestamp().isAfter(currPoint.getTimestamp())
				&& currPoint.getTimestamp().isAfter(prevPoint.getTimestamp()))
			{
				// Calculate average speed between prevPoint and nextPoint
				double rads = DataPoint.calculateRadiansBetween(prevPoint, currPoint)
					+ DataPoint.calculateRadiansBetween(currPoint, nextPoint);
				double time = nextPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp()) / 60.0 / 60.0;
				// Convert to distance and pass to chartseries
				if (Config.getUseMetricUnits()) {
					values.setData(i, Distance.convertRadiansToDistance(rads, Units.KILOMETRES) / time);
				} else {
					values.setData(i, Distance.convertRadiansToDistance(rads, Units.MILES) / time);
				}
			}
		}
		return values;
	}

	/**
	 * Calculate the vertical speed values for each point in the given track
	 * @param inTrack track object
	 * @return vertical speed values in a ChartSeries object
	 */
	private static ChartSeries getVertSpeedValues(Track inTrack)
	{
		// Calculate speeds and fill in in values array
		ChartSeries values = new ChartSeries(inTrack.getNumPoints());
		Altitude.Format altFormat = Config.getUseMetricUnits()?Altitude.Format.METRES:Altitude.Format.FEET;
		DataPoint prevPoint = null, currPoint = null, nextPoint = null;
		DataPoint[] points = getDataPoints(inTrack, true); // require that points have altitudes too
		// Loop over collected points
		for (int i=1; i<(points.length-1); i++)
		{
			prevPoint = points[i-1];
			currPoint = points[i];
			nextPoint = points[i+1];
			if (prevPoint != null && currPoint != null && nextPoint != null
				&& nextPoint.getTimestamp().isAfter(currPoint.getTimestamp())
				&& currPoint.getTimestamp().isAfter(prevPoint.getTimestamp()))
			{
				// Calculate average vertical speed between prevPoint and nextPoint
				double vspeed = (nextPoint.getAltitude().getValue(altFormat) - prevPoint.getAltitude().getValue(altFormat))
				 * 1.0 / nextPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp());
				values.setData(i, vspeed);
			}
		}
		return values;
	}


	/**
	 * Get an array of DataPoints with data for the charts
	 * @param inTrack track object containing points
	 * @param inRequireAltitudes true if only points with altitudes are considered
	 * @return array of points with contiguous non-null elements (<= size) with timestamps
	 */
	private static DataPoint[] getDataPoints(Track inTrack, boolean inRequireAltitudes)
	{
		DataPoint[] points = new DataPoint[inTrack.getNumPoints()];
		DataPoint currPoint = null;
		int pointNum = 0;
		// Loop over all points
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			currPoint = inTrack.getPoint(i);
			if (currPoint != null && !currPoint.isWaypoint() && currPoint.hasTimestamp()
				&& (!inRequireAltitudes || currPoint.hasAltitude()))
			{
				points[pointNum] = currPoint;
				pointNum++;
			}
		}
		// Any elements at the end of the array will stay null
		// Also note, chronological order is not checked
		return points;
	}


	/**
	 * Select a file to write for the SVG output
	 * @return
	 */
	private File selectSvgFile()
	{
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.svg", new String[] {"svg"}));
			_fileChooser.setAcceptAllFileFilterUsed(false);
			// start from directory in config which should be set
			File configDir = Config.getWorkingDirectory();
			if (configDir != null) {_fileChooser.setCurrentDirectory(configDir);}
		}
		boolean chooseAgain = true;
		while (chooseAgain)
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = _fileChooser.getSelectedFile();
				// Check file extension
				if (!file.getName().toLowerCase().endsWith(".svg")) {
					file = new File(file.getAbsolutePath() + ".svg");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || (file.canWrite() && JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION))
				{
					return file;
				}
				else {
					chooseAgain = true;
				}
			}
		}
		// Cancel pressed so no file selected
		return null;
	}


	/**
	 * @param inNumCharts number of charts to draw
	 * @return array of ints describing position and height of each subchart
	 */
	private static int[] getHeights(int inNumCharts)
	{
		if (inNumCharts <= 1) {return new int[] {0, 100};}
		if (inNumCharts == 2) {return new int[] {25, 75, 0, 25};}
		if (inNumCharts == 3) {return new int[] {40, 60, 20, 20, 0, 20};}
		return new int[] {54, 46, 36, 18, 18, 18, 0, 18};
	}

	/**
	 * Prompt the user to set/edit the path to gnuplot
	 */
	private void setGnuplotPath()
	{
		String currPath = Config.getGnuplotPath();
		Object path = JOptionPane.showInputDialog(_dialog,
			I18nManager.getText("dialog.charts.gnuplotpath"),
			I18nManager.getText(getNameKey()),
			JOptionPane.QUESTION_MESSAGE, null, null, "" + currPath);
		if (path != null)
		{
			String pathString = path.toString().trim();
			if (!pathString.equals("") && !pathString.equals(currPath)) {
				Config.setGnuplotPath(pathString);
				// warn if gnuplot still not found
				if (!ExternalTools.isGnuplotInstalled()) {
					_app.showErrorMessage(getNameKey(), "dialog.charts.gnuplotnotfound");
				}
			}
		}
	}
}
