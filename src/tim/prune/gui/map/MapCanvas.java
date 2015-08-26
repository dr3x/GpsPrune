package tim.prune.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.FunctionLibrary;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.gui.IconManager;
import tim.prune.waypoint.GotoManager;

/**
 * Class for the map canvas, to display a background map and draw on it
 */
public abstract class MapCanvas extends JPanel implements MouseListener, MouseMotionListener, DataSubscriber,
	KeyListener, MouseWheelListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/** Constant for pan distance from key presses */
	protected static final int PAN_DISTANCE = 20;

	// Colours
	protected static final Color COLOR_MESSAGES   = Color.GRAY;

	// Drawing modes
	protected static final int MODE_DEFAULT = 0;
	protected static final int MODE_ZOOM_RECT = 1;
	protected static final int MODE_SELECT_RECT = 2;
	
	private App _app;
	/** Tile manager */
	private MapTileManager _tileManager = new MapTileManager(this);
	/** Image to display */
	protected BufferedImage _mapImage = null;
	/** Slider for transparency */
	private JSlider _transparencySlider = null;
	/** Checkbox for scale bar */
	private JCheckBox _scaleCheckBox = null;
	/** Checkbox for maps */
	private JCheckBox _mapCheckBox = null;
	/** Right-click popup menu */
	private JPopupMenu _popup = null;
	/** Top component panel */
	private JPanel _topPanel = null;
	/** Side component panel */
	private JPanel _sidePanel = null;
	/** Scale bar */
	private ScaleBar _scaleBar = null;
	private TileSource _tileSource = null;
	private CursorPosition _cursorPosition = null;
	
	/* Data */
	/** Map position */
	private MapPosition _mapPosition = null;
	/** x coordinate of drag from point */
	protected int _dragFromX = -1;
	/** y coordinate of drag from point */
	protected int _dragFromY = -1;
	/** x coordinate of drag to point */
	protected int _dragToX = -1;
	/** y coordinate of drag to point */
	protected int _dragToY = -1;
	/** x coordinate of popup menu */
	protected int _popupMenuX = -1;
	/** y coordinate of popup menu */
	protected int _popupMenuY = -1;
	/** projection **/
	private Projection projection;
	
	protected boolean _recalculate = false;
	/** Current drawing mode */
	protected int _drawMode = MODE_DEFAULT;


	private JPanel _bottomPanel;
	
	private int[] tileIndices = new int[4];
	private int[] pixelOffsets = new int[2];

	/**
	 * Constructor
	 * @param inApp App object for callbacks
	 * @param inTrackInfo track info object
	 */
	public MapCanvas(App app)
	{
		_app = app;
		
		createMembers();
		attachListeners();

		buildTopPanel();
		buildSidePanel();
		buildBottomPanel();

		// add control panels to this one
		setLayout(new BorderLayout());
		add(_topPanel, BorderLayout.NORTH);
		add(_sidePanel, BorderLayout.WEST);
		add(_bottomPanel, BorderLayout.SOUTH);
		
		// Make popup menu
		buildContextMenu();
	}

	protected void createMembers() {
		_mapPosition = new MapPosition();
	}

	protected void attachListeners() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		_app.getFrame().addKeyListener(this);
	}


	protected JPanel buildSidePanel() {
		// Add zoom in, zoom out buttons
		_sidePanel = new JPanel();
		_sidePanel.setLayout(new BoxLayout(_sidePanel, BoxLayout.Y_AXIS));
		_sidePanel.setOpaque(false);
		JButton zoomInButton = new JButton(IconManager.getImageIcon(IconManager.ZOOM_IN_BUTTON));
		zoomInButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		zoomInButton.setContentAreaFilled(false);
		zoomInButton.setToolTipText(I18nManager.getText("menu.map.zoomin"));
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomIn();
			}
		});
		zoomInButton.setFocusable(false); // stop button from stealing keyboard focus
		_sidePanel.add(zoomInButton);
		JButton zoomOutButton = new JButton(IconManager.getImageIcon(IconManager.ZOOM_OUT_BUTTON));
		zoomOutButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		zoomOutButton.setContentAreaFilled(false);
		zoomOutButton.setToolTipText(I18nManager.getText("menu.map.zoomout"));
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomOut();
			}
		});
		zoomOutButton.setFocusable(false); // stop button from stealing keyboard focus
		_sidePanel.add(zoomOutButton);
		return _sidePanel;
	}


	protected JPanel buildTopPanel() {
		// Make special listener for changes to map checkbox
		ItemListener mapCheckListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				_tileManager.clearMemoryCaches();
				_recalculate = true;
				Config.setConfigBoolean(Config.KEY_SHOW_MAP, e.getStateChange() == ItemEvent.SELECTED);
				UpdateMessageBroker.informSubscribers(); // to let menu know
			}
		};
		_topPanel = new JPanel();
		_topPanel.setLayout(new FlowLayout());
		_topPanel.setOpaque(false);
		// Make slider for transparency
		_transparencySlider = new JSlider(0, 5, 0);
		_transparencySlider.setPreferredSize(new Dimension(100, 20));
		_transparencySlider.setMajorTickSpacing(1);
		_transparencySlider.setSnapToTicks(true);
		_transparencySlider.setOpaque(false);
		_transparencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				_recalculate = true;
				repaint();
			}
		});
		_transparencySlider.setFocusable(false); // stop slider from stealing keyboard focus
		_topPanel.add(_transparencySlider);
		// Add checkbox button for enabling scale bar
		_scaleCheckBox = new JCheckBox(IconManager.getImageIcon(IconManager.SCALEBAR_BUTTON), true);
		_scaleCheckBox.setSelectedIcon(IconManager.getImageIcon(IconManager.SCALEBAR_BUTTON_ON));
		_scaleCheckBox.setOpaque(false);
		_scaleCheckBox.setToolTipText(I18nManager.getText("menu.map.showscalebar"));
		_scaleCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				_scaleBar.setVisible(_scaleCheckBox.isSelected());
			}
		});
		_scaleCheckBox.setFocusable(false); // stop button from stealing keyboard focus
		_topPanel.add(_scaleCheckBox);
		// Add checkbox button for enabling maps or not
		_mapCheckBox = new JCheckBox(IconManager.getImageIcon(IconManager.MAP_BUTTON), false);
		_mapCheckBox.setSelectedIcon(IconManager.getImageIcon(IconManager.MAP_BUTTON_ON));
		_mapCheckBox.setOpaque(false);
		_mapCheckBox.setToolTipText(I18nManager.getText("menu.map.showmap"));
		_mapCheckBox.addItemListener(mapCheckListener);
		_mapCheckBox.setFocusable(false); // stop button from stealing keyboard focus
		_topPanel.add(_mapCheckBox);
		
		return _topPanel;
	}
	
	protected JPanel buildBottomPanel() {
		_bottomPanel = new JPanel();
		_bottomPanel.setLayout(new BoxLayout(_bottomPanel,BoxLayout.X_AXIS));
		_bottomPanel.setOpaque(false);
		
		_scaleBar = new ScaleBar();
		_bottomPanel.add(_scaleBar);
		_tileSource = new TileSource(this, SwingConstants.CENTER);
		_tileSource.setEnabled(Config.getConfigBoolean(Config.KEY_SHOW_TILE_SOURCE));
		_bottomPanel.add(_tileSource);
		_cursorPosition = new CursorPosition(this, SwingConstants.RIGHT);
		_bottomPanel.add(_cursorPosition);
		
		return _bottomPanel;
	}


	/**
	 * Make the popup menu for right-clicking the map
	 */
	protected JPopupMenu buildContextMenu()
	{
		_popup = new JPopupMenu();
		JMenuItem zoomInItem = new JMenuItem(I18nManager.getText("menu.map.zoomin"));
		zoomInItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				panMap((_popupMenuX - getWidth()/2)/2, (_popupMenuY - getHeight()/2)/2);
				zoomIn();
			}});
		_popup.add(zoomInItem);
		JMenuItem zoomOutItem = new JMenuItem(I18nManager.getText("menu.map.zoomout"));
		zoomOutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				panMap(-(_popupMenuX - getWidth()/2), -(_popupMenuY - getHeight()/2));
				zoomOut();
			}});
		_popup.add(zoomOutItem);
		JMenuItem zoomFullItem = new JMenuItem(I18nManager.getText("menu.map.zoomfull"));
		zoomFullItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomToFit();				
			}});
		_popup.add(zoomFullItem);
		_popup.addSeparator();
		// Set background
		JMenuItem setMapBgItem = new JMenuItem(
			I18nManager.getText(FunctionLibrary.FUNCTION_SET_MAP_BG.getNameKey()));
		setMapBgItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				FunctionLibrary.FUNCTION_SET_MAP_BG.begin();
			}});
		_popup.add(setMapBgItem);
		_popup.addSeparator();
		_popup.add(GotoManager.getInstance().getMenu());
		return _popup;
	}


	/**
	 * Paint method
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	public void paint(Graphics inG)
	{
		super.paint(inG);
		projection = new Projection(_mapPosition, this);
		
		if (_mapImage != null && (_mapImage.getWidth() != getWidth() || _mapImage.getHeight() != getHeight())) {
			_mapImage = null;
		}

		// Draw the map contents if necessary
		if ((_mapImage == null || _recalculate))
		{
			paintMapContents().dispose();
			_scaleBar.updateScale(_mapPosition.getZoom(), _mapPosition.getYFromPixels(0, 0));
		}
		// Draw the prepared image onto the panel
		if (_mapImage != null) {
			inG.drawImage(_mapImage, 0, 0, getWidth(), getHeight(), null);
		}
		// Draw the zoom rectangle if necessary
		if (_drawMode == MODE_ZOOM_RECT || _drawMode == MODE_SELECT_RECT)
		{
			inG.setColor(Color.RED);
			inG.drawLine(_dragFromX, _dragFromY, _dragFromX, _dragToY);
			inG.drawLine(_dragFromX, _dragFromY, _dragToX, _dragFromY);
			inG.drawLine(_dragToX, _dragFromY, _dragToX, _dragToY);
			inG.drawLine(_dragFromX, _dragToY, _dragToX, _dragToY);
		}
		
		try {
			paintOverlays((Graphics2D) inG);
		} catch ( Exception ignored ) {}
		
		// Draw slider etc on top
		paintChildren(inG);
	}


	/**
	 * Paint the map tiles and the points on to the _mapImage
	 */
	protected Graphics paintMapContents()
	{
		if (_mapImage == null || _mapImage.getWidth() != getWidth() || _mapImage.getHeight() != getHeight())
		{
			_mapImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}

		// Clear map
		Graphics g = _mapImage.getGraphics();
		// Clear to background
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		// Check whether maps are on or not
		boolean showMap = Config.getConfigBoolean(Config.KEY_SHOW_MAP);
		_mapCheckBox.setSelected(showMap);

		_recalculate = false;
		// Only get map tiles if selected
		if (showMap)
		{
			_tileManager.checkZoom(_mapPosition.getZoom());

			if (_mapImage == null) 
				return g;

			if (_tileManager.isOverzoomed())
			{
				// display overzoom message
				g.setColor(COLOR_MESSAGES);
				g.drawString(I18nManager.getText("map.overzoom"), 50, getHeight()/2);
			}
			else
			{
				int numLayers = _tileManager.getNumLayers();
				// Loop over tiles drawing each one				
				tileIndices = _mapPosition.getTileIndices(getWidth(), getHeight(), tileIndices);
				pixelOffsets = _mapPosition.getDisplayOffsets(getWidth(), getHeight(), pixelOffsets);
				int offX = 0;
				for (int tileX = tileIndices[0]; tileX <= tileIndices[1]; tileX++)
				{
					int x = offX++ * 256 - pixelOffsets[0];
					for (int tileY = tileIndices[2]; tileY <= tileIndices[3]; tileY++)
					{
						int y = (tileY - tileIndices[2]) * 256 - pixelOffsets[1];
						// Loop over layers
						for (int l=0; l<numLayers; l++)
						{
							Image image = _tileManager.getTileImage(l, tileX, tileY);
							if (image != null) {
								g.drawImage(image, x, y, 256, 256, null);
							}
						}
					}
				}

				// Make maps brighter / fainter
				final float[] scaleFactors = {1.0f, 1.05f, 1.1f, 1.2f, 1.6f, 2.2f};
				final float scaleFactor = scaleFactors[_transparencySlider.getValue()];
				if (scaleFactor > 1.0f)
				{
					RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					RescaleOp op = new RescaleOp(scaleFactor, 0, hints);
					op.filter(_mapImage, _mapImage);
				}
			}
		}

		// enable / disable transparency slider
		_transparencySlider.setEnabled(showMap);		
		return g;
	}

	protected void paintOverlays( Graphics2D graphics ) {
		_app.getOverlays().draw(this, graphics);
	}

	/**
	 * Inform that tiles have been updated and the map can be repainted
	 * @param inIsOk true if data loaded ok, false for error
	 */
	public synchronized void tilesUpdated(boolean inIsOk)
	{
		_recalculate = true;
		repaint();
	}

	/**
	 * Zoom out, if not already at minimum zoom
	 */
	public void zoomOut()
	{
		_mapPosition.zoomOut();
		_recalculate = true;
		repaint();
	}

	/**
	 * Zoom in, if not already at maximum zoom
	 */
	public void zoomIn()
	{
		_mapPosition.zoomIn();
		_recalculate = true;
		repaint();
	}

	public int getZoom() {
		return _mapPosition.getZoom();
	}

	public void setZoom(int inZoom) {
		int z = _mapPosition.getZoom();
		if (z == inZoom) return;
		if (z < inZoom) {
			for (int x = z; x < inZoom; x++) {
				_mapPosition.zoomIn();
			}
		} else {
			for (int x = z; x > inZoom; x--) {
				_mapPosition.zoomOut();
			}
		}
		_recalculate = true;
		repaint();
	}

	/**
	 * Pan map
	 * @param inDeltaX x shift
	 * @param inDeltaY y shift
	 */
	public void panMap(int inDeltaX, int inDeltaY)
	{
		_mapPosition.pan(inDeltaX, inDeltaY, getHeight());
		_recalculate = true;
		repaint();
	}

	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize()
	{
		final Dimension minSize = new Dimension(1024, 768);
		return minSize;
	}

	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize()
	{
		return getMinimumSize();
	}


	/**
	 * Respond to mouse click events
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent inE)
	{
		if (!inE.isConsumed())
		{
			 // select point if it's a left-click
			if (!inE.isMetaDown())
			{
				if (inE.getClickCount() == 1) {
					if (GotoManager.getInstance().isDialogOpen()) {
						// Set lat/lon for the goto dialog.
						_popupMenuX = inE.getX();
						_popupMenuY = inE.getY();
						Double fromScreen = projection.fromScreen(_popupMenuX, _popupMenuY, null);
						GotoManager.getInstance().setCurrentLatLon(fromScreen.y, fromScreen.x);
						inE.consume();
					}
				}
				if (inE.getClickCount() == 2) {
					// double click
					if (_drawMode == MODE_DEFAULT) {
						panMap(inE.getX() - getWidth()/2, inE.getY() - getHeight()/2);
						zoomIn();
					}
					else {
						_drawMode = MODE_DEFAULT;
					}
					inE.consume();
				}
			}
			else
			{
				// show the popup menu for right-clicks
				_popupMenuX = inE.getX();
				_popupMenuY = inE.getY();
				if (!GotoManager.getInstance().isDialogOpen()) {
					// Set lat/lon on case they want to add a goto at this
					// point.
					Double fromScreen = projection.fromScreen(_popupMenuX,
							_popupMenuY, null);
					GotoManager.getInstance().setCurrentLatLon(fromScreen.y,
							fromScreen.x);
				}
				_popup.show(this, _popupMenuX, _popupMenuY);
				inE.consume();
			}
		}
	}

	/**
	 * Ignore mouse enter events
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent inE)
	{
		// ignore
	}

	/**
	 * Ignore mouse exited events
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent inE)
	{
		// ignore
	}

	/**
	 * Ignore mouse pressed events
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent inE)
	{
		requestFocus();
	}

	/**
	 * Respond to mouse released events
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent inE)
	{
		if( !inE.isConsumed() ) {
			_recalculate = true;
			if (_drawMode == MODE_ZOOM_RECT && Math.abs(_dragToX - _dragFromX) > 20
					&& Math.abs(_dragToY - _dragFromY) > 20)
			{
				_mapPosition.zoomToPixels(_dragFromX, _dragToX, _dragFromY, _dragToY, getWidth(), getHeight());
				_drawMode = MODE_DEFAULT;
			}
			_dragFromX = _dragFromY = -1;
		}
		repaint();
	}

	/**
	 * Respond to mouse drag events
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent inE)
	{
		if( !inE.isConsumed() ) {
			if(inE.isShiftDown())
			{
				// Shift-click and drag - draw rectangle and control zoom
				_drawMode = MODE_SELECT_RECT;
				if (_dragFromX == -1) {
					_dragFromX = inE.getX();
					_dragFromY = inE.getY();
				}
				_dragToX = inE.getX();
				_dragToY = inE.getY();
				repaint();
			}
			else if (!inE.isMetaDown())
			{
				// Left mouse drag - pan map by appropriate amount
				if (_dragFromX != -1)
				{
					panMap(_dragFromX - inE.getX(), _dragFromY - inE.getY());
					_recalculate = true;
					repaint();
				}
				_dragFromX = _dragToX = inE.getX();
				_dragFromY = _dragToY = inE.getY();
			}
			else
			{
				// Right-click and drag - draw rectangle and control zoom
				_drawMode = MODE_ZOOM_RECT;
				if (_dragFromX == -1) {
					_dragFromX = inE.getX();
					_dragFromY = inE.getY();
				}
				_dragToX = inE.getX();
				_dragToY = inE.getY();
				repaint();
			}
		}
	}

	/**
	 * Respond to mouse move events without button pressed
	 * @param inEvent ignored
	 */
	public void mouseMoved(MouseEvent inEvent)
	{
		_cursorPosition.repaint();
		_tileSource.repaint();
	}

	/**
	 * Respond to status bar message from broker
	 * @param inMessage message, ignored
	 */
	public void actionCompleted(String inMessage)
	{
		// ignore
	}

	/**
	 * Respond to data updated message from broker
	 * @param inUpdateType type of update
	 */
	public void dataUpdated(byte inUpdateType)
	{
		_recalculate = true;
		if ((inUpdateType & DataSubscriber.MAPSERVER_CHANGED) > 0) {
			_tileManager.resetConfig();
		}
		_tileSource.setEnabled(Config.getConfigBoolean(Config.KEY_SHOW_TILE_SOURCE));
		repaint();
		_topPanel.setVisible(true);
		_sidePanel.setVisible(true);
		// grab focus for the key presses
		this.requestFocus();
	}

	/**
	 * Respond to key presses on the map canvas
	 * @param inE key event
	 */
	public void keyPressed(KeyEvent inE)
	{
		if( !inE.isConsumed() ) {
			int code = inE.getKeyCode();
			// Check for Ctrl key (for Linux/Win) or meta key (Clover key for Mac)
			if (inE.isControlDown() || inE.isMetaDown())
			{
				// Check for arrow keys to zoom in and out
				if (code == KeyEvent.VK_UP)
					zoomIn();
				else if (code == KeyEvent.VK_DOWN)
					zoomOut();
			}
			else
			{
				// Check for arrow keys to pan
				int upwardsPan = 0;
				if (code == KeyEvent.VK_UP)
					upwardsPan = -PAN_DISTANCE;
				else if (code == KeyEvent.VK_DOWN)
					upwardsPan = PAN_DISTANCE;
				int rightwardsPan = 0;
				if (code == KeyEvent.VK_RIGHT)
					rightwardsPan = PAN_DISTANCE;
				else if (code == KeyEvent.VK_LEFT)
					rightwardsPan = -PAN_DISTANCE;
				panMap(rightwardsPan, upwardsPan);
				// Check for escape
				if (code == KeyEvent.VK_ESCAPE)
					_drawMode = MODE_DEFAULT;
			}
		}
	}
	
	public abstract void zoomToFit();

	/**
	 * @param inE key released event, ignored
	 */
	public void keyReleased(KeyEvent e)
	{
		// ignore
	}

	/**
	 * @param inE key typed event, ignored
	 */
	public void keyTyped(KeyEvent inE)
	{
		// ignore
	}

	/**
	 * @param inE mouse wheel event indicating scroll direction
	 */
	public void mouseWheelMoved(MouseWheelEvent inE)
	{
		int clicks = inE.getWheelRotation();
		if (clicks < 0) {
			zoomIn();
		}
		else if (clicks > 0) {
			zoomOut();
		}
	}

	/**
	 * @return current map position
	 */
	public MapPosition getMapPosition()
	{
		return _mapPosition;
	}
	
	public MapTileManager getTileManager() {
		return _tileManager;
	}
	
	public Projection getProjection() {
		return projection;
	}

	/**
	 * Center the canvas on the provided lat/lon.
	 * @param inLat
	 * @param inLon
	 */
	public void gotoLatLon(double inLat, double inLon) {
		getMapPosition().gotoLatLon(inLat, inLon);
		_recalculate = true;
		repaint();
	}

	public App getApp() {
		return _app;
	}
}
