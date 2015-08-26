package tim.prune.gui.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D.Double;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.Checker;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.IconManager;
import tim.prune.waypoint.GotoManager;

public class PruneMapCanvas extends MapCanvas
	implements MouseListener, MouseMotionListener, DataSubscriber,
	KeyListener, MouseWheelListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Constant for click sensitivity when selecting nearest point */
	private static final int CLICK_SENSITIVITY = 10;
	/** Constant for pan distance from autopan */
	private static final int AUTOPAN_DISTANCE = 75;
	
	protected static final int MODE_DRAW_POINTS_START = 2;
	protected static final int MODE_DRAW_POINTS_CONT = 3;
	
	/** App object for callbacks */
	private PruneApp _app;
	/** Track object */
	private Track _track;
	/** TrackInfo object */
	private TrackInfo _trackInfo;
	/** Selection object */
	private Selection _selection;
	/** Previously selected point */
	private int _prevSelectedPoint = -1;
	/** Checkbox for autopan */
	private JCheckBox _autopanCheckBox;
	/** Checkbox for connecting track points */
	private JCheckBox _connectCheckBox;
	/** Right-click popup menu */
	private JPopupMenu _popup;
	/** Top component panel */
	private JPanel _topPanel;
	/* Data */
	private DoubleRange _latRange = null, _lonRange = null;
	private DoubleRange _xRange = null, _yRange = null;
	/** Flag to check bounds on next paint */
	private boolean _checkBounds = false;
	
	private JMenuItem _saveSelectionItem;


	/**
	 * Constructor
	 * @param inApp App object for callbacks
	 * @param inTrackInfo track info object
	 */
	public PruneMapCanvas(PruneApp inApp, TrackInfo inTrackInfo)
	{	
		super( inApp );
		_app = inApp;
		_trackInfo = inTrackInfo;		
		_track = inTrackInfo.getTrack();
		_selection = inTrackInfo.getSelection();
	}
	
	@Override
	protected JPanel buildTopPanel() {		
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				_recalculate = true;
				repaint();
			}
		};
		
		_topPanel = super.buildTopPanel();
		
		// Add checkbox button for enabling autopan or not
		_autopanCheckBox = new JCheckBox(IconManager.getImageIcon(IconManager.AUTOPAN_BUTTON), true);
		_autopanCheckBox.setSelectedIcon(IconManager.getImageIcon(IconManager.AUTOPAN_BUTTON_ON));
		_autopanCheckBox.setOpaque(false);
		_autopanCheckBox.setToolTipText(I18nManager.getText("menu.map.autopan"));
		_autopanCheckBox.addItemListener(itemListener);
		_autopanCheckBox.setFocusable(false); // stop button from stealing keyboard focus
		_topPanel.add(_autopanCheckBox);
		// Add checkbox button for connecting points or not
		_connectCheckBox = new JCheckBox(IconManager.getImageIcon(IconManager.POINTS_DISCONNECTED_BUTTON), true);
		_connectCheckBox.setSelectedIcon(IconManager.getImageIcon(IconManager.POINTS_CONNECTED_BUTTON));
		_connectCheckBox.setOpaque(false);
		_connectCheckBox.setToolTipText(I18nManager.getText("menu.map.connect"));
		_connectCheckBox.addItemListener(itemListener);
		_connectCheckBox.setFocusable(false); // stop button from stealing keyboard focus
		_topPanel.add(_connectCheckBox);
		return _topPanel;
	}


	/**
	 * Make the popup menu for right-clicking the map
	 */
	protected JPopupMenu buildContextMenu()
	{
		_popup = super.buildContextMenu();

		_popup.addSeparator();
		// new point option
		JMenuItem newPointItem = new JMenuItem(I18nManager.getText("menu.map.newpoint"));
		newPointItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.createPoint(createPointFromClick(_popupMenuX, _popupMenuY));
			}});
		_popup.add(newPointItem);
		// draw point series
		JMenuItem drawPointsItem = new JMenuItem(I18nManager.getText("menu.map.drawpoints"));
		drawPointsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_drawMode = MODE_DRAW_POINTS_START;
			}
		});
		_popup.add(drawPointsItem);
		// delete point
		_popup.addSeparator();
		JMenuItem deleteItem = new JMenuItem(I18nManager.getText("Delete"));
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if( _selection.getCurrentPointIndex() > -1 ) {
					_app.deleteCurrentPoint();
				} else if( _selection.hasRangeSelected() ) {
					_app.deleteSelectedRange();
				}
			}
		});
		_popup.add(deleteItem);
		_popup.addSeparator();
		_saveSelectionItem = new JMenuItem(I18nManager.getText("menu.range.save"));
		_saveSelectionItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveSelectionAs();
			}
		});
		_popup.add(_saveSelectionItem);
		return _popup;
	}

	public void setSaveRangeEnabled(boolean inFlag) {
		_saveSelectionItem.setEnabled(inFlag);
	}

	/**
	 * Zoom to fit the current data area
	 */
	public void zoomToFit()
	{
		_latRange = _track.getLatRange();
		_lonRange = _track.getLonRange();
		if( _latRange == null || _latRange.isEmpty() || _lonRange == null || _lonRange.isEmpty() ) {
			_latRange = new DoubleRange(-45, 45);
			_lonRange = new DoubleRange(-90, 90);
		}
		
		_xRange = new DoubleRange(MapUtils.getXFromLongitude(_lonRange.getMinimum()),
			MapUtils.getXFromLongitude(_lonRange.getMaximum()));
		_yRange = new DoubleRange(MapUtils.getYFromLatitude(_latRange.getMinimum()),
			MapUtils.getYFromLatitude(_latRange.getMaximum()));
		getMapPosition().zoomToXY(_xRange.getMinimum(), _xRange.getMaximum(), _yRange.getMinimum(), _yRange.getMaximum(),
				getWidth(), getHeight());
		_recalculate = true;
		repaint();
	}


	/**
	 * Paint method
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	public void paint(Graphics inG)
	{
		if (_track.getNumPoints() > 0)
		{
			// Check for autopan if enabled / necessary
			if (_autopanCheckBox.isSelected())
			{
				int selectedPoint = _selection.getCurrentPointIndex();
				if (selectedPoint >= 0 && _dragFromX == -1 && selectedPoint != _prevSelectedPoint)
				{
					int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(selectedPoint));
					int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(selectedPoint));
					int panX = 0;
					int panY = 0;
					if (px < PAN_DISTANCE) {
						panX = px - AUTOPAN_DISTANCE;
					}
					else if (px > (getWidth()-PAN_DISTANCE)) {
						panX = AUTOPAN_DISTANCE + px - getWidth();
					}
					if (py < PAN_DISTANCE) {
						panY = py - AUTOPAN_DISTANCE;
					}
					if (py > (getHeight()-PAN_DISTANCE)) {
						panY = AUTOPAN_DISTANCE + py - getHeight();
					}
					if (panX != 0 || panY != 0) {
						getMapPosition().pan(panX, panY, getHeight());
					}
				}
				_prevSelectedPoint = selectedPoint;
			}
		}
		
		super.paint(inG);
	}


	/**
	 * Paint the map tiles and the points on to the _mapImage
	 */
	protected Graphics paintMapContents()
	{
		Graphics g = super.paintMapContents();

		// Paint the track points on top
		int pointsPainted = 1;
		try
		{
			pointsPainted = paintPoints(g);
		}
		catch (NullPointerException npe) { // ignore, probably due to data being changed during drawing
		}

		// Zoom to fit if no points found
		if (pointsPainted <= 0 && _checkBounds) {
			if (!GotoManager.getInstance().gotoStartView()) {
				zoomToFit();
				_recalculate = true;
				repaint();
			}
		}
		_checkBounds = false;
		
		return g;
	}
	
	@Override
	protected void paintOverlays(Graphics2D graphics) {
		super.paintOverlays(graphics);
		if (_drawMode == MODE_DRAW_POINTS_CONT)
		{
			// draw line to mouse position to show drawing mode
			graphics.setColor(Config.getColourScheme().getColour(ColourScheme.IDX_POINT));
			int prevIndex = _track.getNumPoints()-1;
			int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(prevIndex));
			int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(prevIndex));
			graphics.drawLine(px, py, _dragToX, _dragToY);
		}
	}

	/**
	 * Paint the points using the given graphics object
	 * @param inG Graphics object to use for painting
	 * @return number of points painted, if any
	 */
	private int paintPoints(Graphics inG)
	{
		// Set up colours
		final Color pointColour = Config.getColourScheme().getColour(ColourScheme.IDX_POINT);
		final Color rangeColour = Config.getColourScheme().getColour(ColourScheme.IDX_SELECTION);
		final Color currentColour = Config.getColourScheme().getColour(ColourScheme.IDX_PRIMARY);
		final Color textColour = Config.getColourScheme().getColour(ColourScheme.IDX_TEXT);

		// try to set line width for painting
		if (inG instanceof Graphics2D)
		{
			int lineWidth = Config.getConfigInt(Config.KEY_LINE_WIDTH);
			if (lineWidth < 1 || lineWidth > 4) {lineWidth = 2;}
			((Graphics2D) inG).setStroke(new BasicStroke(lineWidth));
		}
		int pointsPainted = 0;
		// draw track points
		inG.setColor(pointColour);
		int prevX = -1, prevY = -1;
		boolean connectPoints = _connectCheckBox.isSelected();
		boolean prevPointVisible = false, currPointVisible = false;
		boolean anyWaypoints = false;
		boolean isWaypoint = false;
		for (int i=0; i<_track.getNumPoints(); i++)
		{
			Color c = _track.getColor(i);
			c = c == null ? pointColour : c;
			inG.setColor(c);
			int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(i));
			int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(i));
			currPointVisible = px >= 0 && px < getWidth() && py >= 0 && py < getHeight();
			isWaypoint = _track.getPoint(i).isWaypoint();
			anyWaypoints = anyWaypoints || isWaypoint;
			if (currPointVisible)
			{
				if (!isWaypoint)
				{
					// Draw rectangle for track point
					if (_track.getPoint(i).getDeleteFlag()) {
						inG.setColor(currentColour);
					}
					else {
						inG.setColor(c);
					}
					inG.drawRect(px-2, py-2, 3, 3);
					pointsPainted++;
				}
			}
			if (!isWaypoint)
			{
				// Connect track points if either of them are visible
				if (connectPoints && (currPointVisible || prevPointVisible)
				 && !(prevX == -1 && prevY == -1)
				 && !_track.getPoint(i).getSegmentStart())
				{
					inG.drawLine(prevX, prevY, px, py);
				}
				prevX = px; prevY = py;
			}
			prevPointVisible = currPointVisible;
		}

		// Loop over points, just drawing blobs for waypoints
		inG.setColor(textColour);
		FontMetrics fm = inG.getFontMetrics();
		int nameHeight = fm.getHeight();
		int width = getWidth();
		int height = getHeight();
		if (anyWaypoints) {
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				if (_track.getPoint(i).isWaypoint())
				{
					int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(i));
					int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(i));
					if (px >= 0 && px < getWidth() && py >= 0 && py < getHeight())
					{
						inG.fillRect(px-3, py-3, 6, 6);
						pointsPainted++;
					}
				}
			}
			// Loop over points again, now draw names for waypoints
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				if (_track.getPoint(i).isWaypoint())
				{
					int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(i));
					int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(i));
					if (px >= 0 && px < getWidth() && py >= 0 && py < getHeight())
					{
						// Figure out where to draw waypoint name so it doesn't obscure track
						String waypointName = _track.getPoint(i).getWaypointName();
						int nameWidth = fm.stringWidth(waypointName);
						boolean drawnName = false;
						// Make arrays for coordinates right left up down
						int[] nameXs = {px + 2, px - nameWidth - 2, px - nameWidth/2, px - nameWidth/2};
						int[] nameYs = {py + (nameHeight/2), py + (nameHeight/2), py - 2, py + nameHeight + 2};
						for (int extraSpace = 4; extraSpace < 13 && !drawnName; extraSpace+=2)
						{
							// Shift arrays for coordinates right left up down
							nameXs[0] += 2; nameXs[1] -= 2;
							nameYs[2] -= 2; nameYs[3] += 2;
							// Check each direction in turn right left up down
							for (int a=0; a<4; a++)
							{
								if (nameXs[a] > 0 && (nameXs[a] + nameWidth) < width
									&& nameYs[a] < height && (nameYs[a] - nameHeight) > 0
									&& !overlapsPoints(nameXs[a], nameYs[a], nameWidth, nameHeight, textColour))
								{
									// Found a rectangle to fit - draw name here and quit
									inG.drawString(waypointName, nameXs[a], nameYs[a]);
									drawnName = true;
									break;
								}
							}
						}
					}
				}
			}
		}

		// Draw selected range
		if (_selection.hasRangeSelected())
		{
			inG.setColor(rangeColour);
			for (int i=_selection.getStart(); i<=_selection.getEnd(); i++)
			{
				int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(i));
				int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(i));
				inG.drawRect(px-1, py-1, 2, 2);
			}
		}

		// Draw selected point, crosshairs
		int selectedPoint = _selection.getCurrentPointIndex();
		if (selectedPoint >= 0)
		{
			int px = getWidth() / 2 + getMapPosition().getXFromCentre(_track.getX(selectedPoint));
			int py = getHeight() / 2 + getMapPosition().getYFromCentre(_track.getY(selectedPoint));
			inG.setColor(currentColour);
			// crosshairs
			inG.drawLine(px, 0, px, getHeight());
			inG.drawLine(0, py, getWidth(), py);
			// oval
			inG.drawOval(px - 2, py - 2, 4, 4);
			inG.drawOval(px - 3, py - 3, 6, 6);
		}
		// Return the number of points painted
		return pointsPainted;
	}


	/**
	 * Tests whether there are any dark pixels within the specified x,y rectangle
	 * @param inX left X coordinate
	 * @param inY bottom Y coordinate
	 * @param inWidth width of rectangle
	 * @param inHeight height of rectangle
	 * @param inTextColour colour of text
	 * @return true if the rectangle overlaps stuff too close to the given colour
	 */
	private boolean overlapsPoints(int inX, int inY, int inWidth, int inHeight, Color inTextColour)
	{
		// each of the colour channels must be further away than this to count as empty
		final int BRIGHTNESS_LIMIT = 80;
		final int textRGB = inTextColour.getRGB();
		final int textLow = textRGB & 255;
		final int textMid = (textRGB >> 8) & 255;
		final int textHigh = (textRGB >> 16) & 255;
		try
		{
			// loop over x coordinate of rectangle
			for (int x=0; x<inWidth; x++)
			{
				// loop over y coordinate of rectangle
				for (int y=0; y<inHeight; y++)
				{
					int pixelColor = _mapImage.getRGB(inX + x, inY - y);
					// split into four components rgba
					int pixLow = pixelColor & 255;
					int pixMid = (pixelColor >> 8) & 255;
					int pixHigh = (pixelColor >> 16) & 255;
					//int fourthBit = (pixelColor >> 24) & 255; // alpha ignored
					// If colours are too close in any channel then it's an overlap
					if (Math.abs(pixLow-textLow) < BRIGHTNESS_LIMIT ||
						Math.abs(pixMid-textMid) < BRIGHTNESS_LIMIT ||
						Math.abs(pixHigh-textHigh) < BRIGHTNESS_LIMIT) {return true;}
				}
			}
		}
		catch (NullPointerException e) {
			// ignore null pointers, just return false
		}
		return false;
	}

	/**
	 * Create a DataPoint object from the given click coordinates
	 * @param inX x coordinate of click
	 * @param inY y coordinate of click
	 * @return DataPoint with given coordinates and no altitude
	 */
	private DataPoint createPointFromClick(int inX, int inY)
	{
		double lat = MapUtils.getLatitudeFromY(getMapPosition().getYFromPixels(inY, getHeight()));
		double lon = MapUtils.getLongitudeFromX(getMapPosition().getXFromPixels(inX, getWidth()));
		return new DataPoint(new Latitude(lat, Coordinate.FORMAT_NONE),
			new Longitude(lon, Coordinate.FORMAT_NONE), null);
	}


	/**
	 * Respond to mouse click events
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent inE)
	{
		super.mouseClicked(inE);

		if (!inE.isConsumed() && !inE.isMetaDown() && inE.getClickCount() == 1)
		{
			if (_drawMode == MODE_DRAW_POINTS_START)
			{
				_app.createPoint(createPointFromClick(inE.getX(), inE.getY()));
				_dragToX = inE.getX();
				_dragToY = inE.getY();
				_drawMode = MODE_DRAW_POINTS_CONT;
			}
			else if (_drawMode == MODE_DRAW_POINTS_CONT)
			{
				DataPoint point = createPointFromClick(inE.getX(), inE.getY());
				_app.createPoint(point);
				point.setSegmentStart(false);
			}
		}
		if (_track != null && _track.getNumPoints() > 0 && !inE.isConsumed())
		{
			 // select point if it's a left-click
			if (!inE.isMetaDown())
			{
				if (inE.getClickCount() == 1)
				{
					// single click
					if (_drawMode == MODE_DEFAULT)
					{
						int pointIndex = _track.getNearestPointIndex(
								getMapPosition().getXFromPixels(inE.getX(), getWidth()),
								getMapPosition().getYFromPixels(inE.getY(), getHeight()),
								getMapPosition().getBoundsFromPixels(CLICK_SENSITIVITY), false);
						// Extend selection for shift-click
						if (inE.isShiftDown()) {
							_trackInfo.extendSelection(pointIndex);
						}
						else {
							_trackInfo.selectPoint(pointIndex);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Respond to mouse move events without button pressed
	 * @param inEvent ignored
	 */
	public void mouseMoved(MouseEvent inEvent)
	{
		if( !inEvent.isConsumed() ) {
			// Ignore unless we're drawing points
			if (_drawMode == MODE_DRAW_POINTS_CONT)
			{
				_dragToX = inEvent.getX();
				_dragToY = inEvent.getY();
				repaint();
			}
		}
		
		super.mouseMoved(inEvent);
	}
	
	/**
	 * Respond to mouse released events
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent inE)
	{
		if( !inE.isConsumed() ) {
			_recalculate = true;
			if (_drawMode == MODE_SELECT_RECT && Math.abs(_dragToX - _dragFromX) > 20
					&& Math.abs(_dragToY - _dragFromY) > 20)
			{
				Double p1 = getProjection().fromScreen(_dragFromX, _dragFromY, null);
				Double p2 = getProjection().fromScreen(_dragToX, _dragToY, null);
				double north = Math.max(p1.y, p2.y);
				double south = Math.min(p1.y, p2.y);
				double east = Math.max(p1.x, p2.x);
				double west = Math.min(p1.x, p2.x);
				
				int[] rangeIndexWithin = _track.getRangeIndexWithin(east, north, west, south, null);
				if( rangeIndexWithin[0] > -1 && rangeIndexWithin[1] > -1 ) {
					_selection.selectRange(rangeIndexWithin[0], rangeIndexWithin[1]);
				}
				_drawMode = MODE_DEFAULT;
			}
			_dragFromX = _dragFromY = -1;
		}
		super.mouseReleased(inE);
	}

	/**
	 * Respond to data updated message from broker
	 * @param inUpdateType type of update
	 */
	public void dataUpdated(byte inUpdateType)
	{
		super.dataUpdated(inUpdateType);
		if ((inUpdateType & DataSubscriber.DATA_ADDED_OR_REMOVED) > 0) {
			_checkBounds = true;
		}
	}

	/**
	 * Respond to key presses on the map canvas
	 * @param inE key event
	 */
	public void keyPressed(KeyEvent inE)
	{
		super.keyPressed(inE);
		
		int code = inE.getKeyCode();
		int currPointIndex = _selection.getCurrentPointIndex();
		// Check for Ctrl key (for Linux/Win) or meta key (Clover key for Mac)
		if (inE.isControlDown() || inE.isMetaDown())
		{
			// Check for arrow keys to zoom in and out
			if (code == KeyEvent.VK_LEFT && currPointIndex > 0)
				_trackInfo.selectPoint(currPointIndex-1);
			else if (code == KeyEvent.VK_RIGHT)
				_trackInfo.selectPoint(currPointIndex+1);
			else if (code == KeyEvent.VK_PAGE_UP)
				_trackInfo.selectPoint(Checker.getPreviousSegmentStart(
					_trackInfo.getTrack(), _trackInfo.getSelection().getCurrentPointIndex()));
			else if (code == KeyEvent.VK_PAGE_DOWN)
				_trackInfo.selectPoint(Checker.getNextSegmentStart(
					_trackInfo.getTrack(), _trackInfo.getSelection().getCurrentPointIndex()));
			// Check for home and end
			else if (code == KeyEvent.VK_HOME)
				_trackInfo.selectPoint(0);
			else if (code == KeyEvent.VK_END)
				_trackInfo.selectPoint(_trackInfo.getTrack().getNumPoints()-1);
		}
		else
		{
			// Check for arrow keys to pan
			// Check for escape
			if (code == KeyEvent.VK_ESCAPE)
				_drawMode = MODE_DEFAULT;
			// Check for backspace key to delete current point (delete key already handled by menu)
			else if (code == KeyEvent.VK_BACK_SPACE && currPointIndex >= 0) {
				_app.deleteCurrentPoint();
			}
		}
	}
}
