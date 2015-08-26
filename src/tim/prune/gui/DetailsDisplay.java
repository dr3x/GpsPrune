package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EtchedBorder;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.IntegerRange;
import tim.prune.data.Selection;
import tim.prune.data.TrackInfo;

/**
 * Class to hold point details and selection details
 * as a visual component
 */
public class DetailsDisplay extends GenericDisplay
{
	private static final long serialVersionUID = -1431760398005648168L;
	private boolean _ignoreScrollEvents = false;

	// Point details
	private JPanel _pointDetailsPanel = null;
	private JLabel _indexLabel = null;
	private JLabel _latTitle = null, _longTitle = null;
	private JLabel _latLabel = null, _longLabel = null;
	private JLabel _mgrsTitle = null, _mgrsLabel = null;
	private JLabel _altLabel = null;
	private JLabel _timeLabel = null, _speedLabel = null;
	private JLabel _nameTitle = null, _typeTitle = null;
	private JLabel _nameLabel = null, _typeLabel = null;
	// Scroll bar
	private JScrollBar _scroller = null;

	// Range details
	private JPanel _rangeDetailsPanel = null;
	private JLabel _rangeLabel = null;
	private JLabel _distanceLabel = null;
	private JLabel _durationLabel = null;
	private JLabel _altMinLabel = null, _altMaxLabel = null;
	private JLabel _climbLabel = null, _descentLabel = null;
	private JLabel _aveSpeedLabel = null;

	// Units
	private JComboBox _coordFormatDropdown = null;
	private JComboBox _distUnitsDropdown = null;
	// Formatter
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	// Cached labels
	private static final String LABEL_POINT_SELECTED = I18nManager.getText("details.index.selected") + ": ";
	private static final String LABEL_POINT_LATITUDE = I18nManager.getText("fieldname.latitude") + ": ";
	private static final String LABEL_POINT_LONGITUDE = I18nManager.getText("fieldname.longitude") + ": ";
	private static final String LABEL_POINT_MGRS = I18nManager.getText("fieldname.mgrs") + ": ";
	private static final String LABEL_POINT_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_POINT_TIMESTAMP = I18nManager.getText("fieldname.timestamp") + ": ";
	private static final String LABEL_POINT_WAYPOINTNAME = I18nManager.getText("fieldname.waypointname") + ": ";
	private static final String LABEL_POINT_WAYPOINTTYPE = I18nManager.getText("fieldname.waypointtype") + ": ";
	private static final String LABEL_POINT_SPEED = I18nManager.getText("fieldname.speed") + ": ";
	private static final String LABEL_RANGE_SELECTED = I18nManager.getText("details.range.selected") + ": ";
	private static final String LABEL_RANGE_DURATION = I18nManager.getText("fieldname.duration") + ": ";
	private static final String LABEL_RANGE_DISTANCE = I18nManager.getText("fieldname.distance") + ": ";
	private static final String LABEL_RANGE_AVERAGE_SPEED = I18nManager.getText("details.range.avespeed") + ": ";
	private static final String LABEL_RANGE_MIN_ALTITUDE = I18nManager.getText("fieldname.altitude.min") + ": ";
	private static final String LABEL_RANGE_MAX_ALTITUDE = I18nManager.getText("fieldname.altitude.max") + ": ";
	private static final String LABEL_RANGE_CLIMB = I18nManager.getText("details.range.climb") + ": ";
	private static final String LABEL_RANGE_DESCENT = I18nManager.getText("details.range.descent") + ": ";
	private static String LABEL_POINT_ALTITUDE_UNITS = null;
	private static Altitude.Format LABEL_POINT_ALTITUDE_FORMAT = Altitude.Format.NO_FORMAT;
	// scrollbar interval
	private static final int SCROLLBAR_INTERVAL = 50;


	/**
	 * Constructor
	 * @param inTrackInfo Track info object
	 */
	public DetailsDisplay(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		Font biggerFont = new JLabel().getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);

		// Point details panel
		_pointDetailsPanel = makePointsDetailsPanel();

		// range details panel
		_rangeDetailsPanel = makeRangeDetailsPanel();

		// add the details panels to the main panel
		c.gridx = 0;
		c.gridy = 0;
		mainPanel.add(Box.createVerticalStrut(5), c);
		c.gridy++;
		mainPanel.add(_pointDetailsPanel,c );
		c.gridy++;
		mainPanel.add(_rangeDetailsPanel, c);
		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);

		// Add format, units selection
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		JLabel coordFormatLabel = new JLabel(I18nManager.getText("details.coordformat") + ": ");
		coordFormatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerPanel.add(coordFormatLabel);
		String[] coordFormats = {I18nManager.getText("units.mgrs"), 
			I18nManager.getText("units.original"), I18nManager.getText("units.degminsec"),
			I18nManager.getText("units.degmin"), I18nManager.getText("units.deg")};
		_coordFormatDropdown = new JComboBox(coordFormats);
		_coordFormatDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dataUpdated(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_coordFormatDropdown);
		_coordFormatDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel unitsLabel = new JLabel(I18nManager.getText("details.distanceunits") + ": ");
		unitsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerPanel.add(unitsLabel);
		String[] distUnits = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.miles")};
		_distUnitsDropdown = new JComboBox(distUnits);
		if (!Config.getConfigBoolean(Config.KEY_METRIC_UNITS)) {_distUnitsDropdown.setSelectedIndex(1);}
		_distUnitsDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Config.setConfigBoolean(Config.KEY_METRIC_UNITS, _distUnitsDropdown.getSelectedIndex() == 0);
				UpdateMessageBroker.informSubscribers(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_distUnitsDropdown);
		_distUnitsDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(lowerPanel, BorderLayout.SOUTH);
	}

	/**
	 * Select the specified point
	 * @param inValue value to select
	 */
	private void selectPoint(int inValue)
	{
		if (_track != null && !_ignoreScrollEvents) {
			_trackInfo.selectPoint(inValue);
		}
	}

	/**
	 * Notification that Track has been updated
	 * @param inUpdateType byte to specify what has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Update current point data, if any
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		Selection selection = _trackInfo.getSelection();
		if ((inUpdateType | DATA_ADDED_OR_REMOVED) > 0) selection.markInvalid();
		int currentPointIndex = selection.getCurrentPointIndex();
		_speedLabel.setText("");
		Distance.Units distUnits = _distUnitsDropdown.getSelectedIndex()==0?Distance.Units.KILOMETRES:Distance.Units.MILES;
		String distUnitsStr = I18nManager.getText(_distUnitsDropdown.getSelectedIndex()==0?"units.kilometres.short":"units.miles.short");
		String speedUnitsStr = I18nManager.getText(_distUnitsDropdown.getSelectedIndex()==0?"units.kmh":"units.mph");
		if (_track == null || currentPoint == null)
		{
			_pointDetailsPanel.setVisible(false);
			_indexLabel.setText(I18nManager.getText("details.nopointselection"));
			_latLabel.setText("");
			_longLabel.setText("");
			_altLabel.setText("");
			_timeLabel.setText("");
			_nameLabel.setText("");
			_typeLabel.setText("");
		}
		else
		{
			_pointDetailsPanel.setVisible(true);
			_indexLabel.setText((currentPointIndex+1) + " " + I18nManager.getText("details.index.of")
				+ " " + _track.getNumPoints());
			if (_coordFormatDropdown.getSelectedIndex() == 0) { // MGRS
				LatLonPoint llp = new LatLonPoint(
						currentPoint.getLatitude().getDouble(),
						currentPoint.getLongitude().getDouble());
				MGRSPoint mgrsPoint = new MGRSPoint();
				MGRSPoint.LLtoMGRS(llp, mgrsPoint);
				_latTitle.setVisible(false);  _latLabel.setVisible(false);
				_longTitle.setVisible(false);  _longLabel.setVisible(false);
				_mgrsTitle.setVisible(true);  _mgrsLabel.setVisible(true);
				_mgrsLabel.setText(mgrsPoint.getPrettyMGRS());
			} else {
				_latTitle.setVisible(true);  _latLabel.setVisible(true);
				_longTitle.setVisible(true);  _longLabel.setVisible(true);
				_mgrsTitle.setVisible(false);  _mgrsLabel.setVisible(false);
				_latLabel.setText(makeCoordinateLabel("", currentPoint.getLatitude(), _coordFormatDropdown.getSelectedIndex()));
				_longLabel.setText(makeCoordinateLabel("", currentPoint.getLongitude(), _coordFormatDropdown.getSelectedIndex()));
			}
			_altLabel.setText(currentPoint.hasAltitude()?
				(currentPoint.getAltitude().getValue() + getAltitudeUnitsLabel(currentPoint.getAltitude().getFormat()))
				:"");
			if (currentPoint.getTimestamp().isValid())
			{
				if (currentPointIndex > 0 && currentPointIndex < (_trackInfo.getTrack().getNumPoints()-1))
				{
					DataPoint prevPoint = _trackInfo.getTrack().getPoint(currentPointIndex - 1);
					DataPoint nextPoint = _trackInfo.getTrack().getPoint(currentPointIndex + 1);
					if (prevPoint.getTimestamp().isValid() && nextPoint.getTimestamp().isValid())
					{
						// use total distance and total time between neighboring points
						long diff = nextPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp());
						if (diff < 1000 && diff > 0)
						{
							double rads = DataPoint.calculateRadiansBetween(prevPoint, currentPoint) +
								DataPoint.calculateRadiansBetween(currentPoint, nextPoint);
							double dist = Distance.convertRadiansToDistance(rads, distUnits);
							String speed = roundedNumber(3600 * dist / diff) + " " + speedUnitsStr;
							_speedLabel.setText(speed);
						}
					}
				}
				_timeLabel.setText(currentPoint.getTimestamp().getText());
			}
			else {
				_timeLabel.setText("");
			}
			// Waypoint name
			final String name = currentPoint.getWaypointName();
			if (name != null && !name.equals(""))
			{
				_nameLabel.setText(name);
				_nameLabel.setVisible(true);
				_nameTitle.setVisible(true);
			}
			else {
				_nameLabel.setText("");
				_nameLabel.setVisible(false);
				_nameTitle.setVisible(false);
			}
			// Waypoint type
			final String type = currentPoint.getFieldValue(Field.WAYPT_TYPE);
			if (type != null && !type.equals("")) {
				_typeLabel.setText(type);
				_typeLabel.setVisible(true);
				_typeTitle.setVisible(true);
			}
			else {
				_typeLabel.setText("");
				_typeLabel.setVisible(false);
				_typeTitle.setVisible(false);
			}
		}

		// Update range details
		if (_track == null || !selection.hasRangeSelected())
		{
			_rangeDetailsPanel.setVisible(false);
			_rangeLabel.setText(I18nManager.getText("details.norangeselection"));
			_distanceLabel.setText("");
			_durationLabel.setText("");
			_altMinLabel.setText("");
			_altMaxLabel.setText("");
			_climbLabel.setText("");
			_descentLabel.setText("");
			_aveSpeedLabel.setText("");
		}
		else
		{
			_rangeDetailsPanel.setVisible(true);
			_rangeLabel.setText((selection.getStart()+1) + " " + I18nManager.getText("details.range.to")
				+ " " + (selection.getEnd()+1));
			_distanceLabel.setText(roundedNumber(selection.getDistance(distUnits)) + " " + distUnitsStr);
			if (selection.getNumSeconds() > 0)
			{
				_durationLabel.setText(DisplayUtils.buildDurationString(selection.getNumSeconds()));
				_aveSpeedLabel.setText(roundedNumber(selection.getDistance(distUnits)/selection.getNumSeconds()*3600.0) + " " + speedUnitsStr);
			}
			else {
				_durationLabel.setText("");
				_aveSpeedLabel.setText("");
			}
			String altUnitsLabel = getAltitudeUnitsLabel(selection.getAltitudeFormat());
			IntegerRange altRange = selection.getAltitudeRange();
			if (altRange.getMinimum() >= 0 && altRange.getMaximum() >= 0)
			{
				_altMinLabel.setText(altRange.getMinimum() + altUnitsLabel);
				_altMaxLabel.setText(altRange.getMaximum() + altUnitsLabel);
				_climbLabel.setText(selection.getClimb() + altUnitsLabel);
				_descentLabel.setText(selection.getDescent() + altUnitsLabel);
			}
			else
			{
				_altMinLabel.setText("");
				_altMaxLabel.setText("");
				_climbLabel.setText("");
				_descentLabel.setText("");
			}
		}

		// Update scroller settings
		_ignoreScrollEvents = true;
		if (_track == null || _track.getNumPoints() < 2)
		{
			// careful to avoid event loops here
			// _scroller.setValue(0);
			_scroller.setEnabled(false);
		}
		else
		{
			_scroller.setMaximum(_track.getNumPoints() -1 + SCROLLBAR_INTERVAL);
			if (currentPointIndex >= 0)
				_scroller.setValue(currentPointIndex);
			_scroller.setEnabled(true);
		}
		_ignoreScrollEvents = false;
	}


	/**
	 * Choose the appropriate altitude units label for the specified format
	 * @param inFormat altitude format
	 * @return language-sensitive string
	 */
	private static String getAltitudeUnitsLabel(Altitude.Format inFormat)
	{
		if (inFormat == LABEL_POINT_ALTITUDE_FORMAT && LABEL_POINT_ALTITUDE_UNITS != null)
			return LABEL_POINT_ALTITUDE_UNITS;
		LABEL_POINT_ALTITUDE_FORMAT = inFormat;
		if (inFormat == Altitude.Format.METRES)
			return " " + I18nManager.getText("units.metres.short");
		return " " + I18nManager.getText("units.feet.short");
	}


	/**
	 * Construct an appropriate coordinate label using the selected format
	 * @param inPrefix prefix of label
	 * @param inCoordinate coordinate
	 * @param inFormat index of format selection dropdown
	 * @return language-sensitive string
	 */
	private static String makeCoordinateLabel(String inPrefix, Coordinate inCoordinate, int inFormat)
	{
		String coord = null;
		switch (inFormat) {
			case 2: // degminsec
				coord = inCoordinate.output(Coordinate.FORMAT_DEG_MIN_SEC); break;
			case 3: // degmin
				coord = inCoordinate.output(Coordinate.FORMAT_DEG_MIN); break;
			case 4: // degrees
				coord = inCoordinate.output(Coordinate.FORMAT_DEG); break;
			default: // just as it was
				coord = inCoordinate.output(Coordinate.FORMAT_NONE);
		}
		// Fix broken degree signs (due to unicode mangling)
		final char brokenDeg = 65533;
		if (coord.indexOf(brokenDeg) >= 0) {
			coord = coord.replaceAll(String.valueOf(brokenDeg), "\u00B0");
		}
		return inPrefix + restrictDP(coord);
	}


	/**
	 * Format a number to a sensible precision
	 * @param inDist distance
	 * @return formatted String
	 */
	private String roundedNumber(double inDist)
	{
		// Set precision of formatter
		int numDigits = 0;
		if (inDist < 1.0)
			numDigits = 3;
		else if (inDist < 10.0)
			numDigits = 2;
		else if (inDist < 100.0)
			numDigits = 1;
		// set formatter
		_distanceFormatter.setMaximumFractionDigits(numDigits);
		_distanceFormatter.setMinimumFractionDigits(numDigits);
		return _distanceFormatter.format(inDist);
	}

	/**
	 * Restrict the given coordinate to a limited number of decimal places for display
	 * @param inCoord coordinate string
	 * @return chopped string
	 */
	private static String restrictDP(String inCoord)
	{
		final int DECIMAL_PLACES = 7;
		if (inCoord == null) return "";
		final int dotPos = Math.max(inCoord.lastIndexOf('.'), inCoord.lastIndexOf(','));
		if (dotPos >= 0) {
			final int chopPos = dotPos + DECIMAL_PLACES;
			if (chopPos < (inCoord.length()-1)) {
				return inCoord.substring(0, chopPos);
			}
		}
		return inCoord;
	}

	/**
	 * Make a point details subpanel
	 * @return panel with correct layout, label
	 */
	private JPanel makePointsDetailsPanel() {
		Font biggerFont = new JLabel().getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		detailsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		JLabel detailsLabel = new JLabel(I18nManager.getText("details.pointdetails"));
		detailsLabel.setFont(biggerFont);
		c.gridwidth = 2;
		detailsPanel.add(detailsLabel, c);
		c.gridy++;
		c.gridwidth = 1;

		detailsPanel.add(new JLabel(LABEL_POINT_SELECTED), c);
		c.gridx++;
		_indexLabel = new JLabel(
				I18nManager.getText("details.nopointselection"));
		detailsPanel.add(_indexLabel, c);

		c.gridx = 0; c.gridy++;
		_latTitle = new JLabel(LABEL_POINT_LATITUDE);
		detailsPanel.add(_latTitle, c);
		c.gridx++;
		_latLabel = new JLabel("");
		detailsPanel.add(_latLabel, c);

		c.gridx = 0; c.gridy++;
		_longTitle = new JLabel(LABEL_POINT_LONGITUDE);
		detailsPanel.add(_longTitle, c);
		c.gridx++;
		_longLabel = new JLabel("");
		detailsPanel.add(_longLabel, c);
		
		c.gridx = 0; c.gridy++;
		_mgrsTitle = new JLabel(LABEL_POINT_MGRS);
		detailsPanel.add(_mgrsTitle, c);
		c.gridx++;
		_mgrsLabel = new JLabel("");
		detailsPanel.add(_mgrsLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_POINT_ALTITUDE), c);
		c.gridx++;
		_altLabel = new JLabel("");
		detailsPanel.add(_altLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_POINT_TIMESTAMP), c);
		c.gridx++;
		_timeLabel = new JLabel("");
		_timeLabel.setMinimumSize(new Dimension(120, 10));
		detailsPanel.add(_timeLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_POINT_SPEED), c);
		c.gridx++;
		_speedLabel = new JLabel("");
		detailsPanel.add(_speedLabel, c);

		c.gridx = 0; c.gridy++;
		_nameTitle = new JLabel(LABEL_POINT_WAYPOINTNAME);
		detailsPanel.add(_nameTitle, c);
		c.gridx++;
		_nameLabel = new JLabel("");
		detailsPanel.add(_nameLabel, c);

		c.gridx = 0; c.gridy++;
		_typeTitle = new JLabel(LABEL_POINT_WAYPOINTTYPE);
		detailsPanel.add(_typeTitle, c);
		c.gridx++;
		_typeLabel = new JLabel("");
		detailsPanel.add(_typeLabel, c);

		c.gridwidth = 2;
		c.gridx = 0; c.gridy++;
		detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		detailsPanel.add(Box.createVerticalStrut(3), c);
		// Scroll bar
		_scroller = new JScrollBar(JScrollBar.HORIZONTAL, 0, SCROLLBAR_INTERVAL, 0, 100);
		_scroller.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				selectPoint(e.getValue());
			}
		});
		_scroller.setEnabled(false);
		c.gridy++;
		detailsPanel.add(_scroller, c);

		return detailsPanel;
	}

	/**
	 * Make a range details subpanel
	 * @return panel with correct layout, label
	 */
	private JPanel makeRangeDetailsPanel() {
		Font biggerFont = new JLabel().getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		detailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel detailsLabel = new JLabel(I18nManager.getText("details.rangedetails"));
		detailsLabel.setFont(biggerFont);
		c.gridwidth = 2;
		detailsPanel.add(detailsLabel, c);
		c.gridy++;
		c.gridwidth = 1;

		detailsPanel.add(new JLabel(LABEL_RANGE_SELECTED), c);
		c.gridx++;
		_rangeLabel = new JLabel(I18nManager.getText("details.norangeselection"));
		detailsPanel.add(_rangeLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_DISTANCE), c);
		c.gridx++;
		_distanceLabel = new JLabel("");
		detailsPanel.add(_distanceLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_DURATION), c);
		c.gridx++;
		_durationLabel = new JLabel("");
		detailsPanel.add(_durationLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_AVERAGE_SPEED), c);
		c.gridx++;
		_aveSpeedLabel = new JLabel("");
		detailsPanel.add(_aveSpeedLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_MIN_ALTITUDE), c);
		c.gridx++;
		_altMinLabel = new JLabel("");
		detailsPanel.add(_altMinLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_MAX_ALTITUDE), c);
		c.gridx++;
		_altMaxLabel = new JLabel("");
		detailsPanel.add(_altMaxLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_CLIMB), c);
		c.gridx++;
		_climbLabel = new JLabel("");
		detailsPanel.add(_climbLabel, c);

		c.gridx = 0; c.gridy++;
		detailsPanel.add(new JLabel(LABEL_RANGE_DESCENT), c);
		c.gridx++;
		_descentLabel = new JLabel("");
		detailsPanel.add(_descentLabel, c);
		detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		return detailsPanel;
	}
}
