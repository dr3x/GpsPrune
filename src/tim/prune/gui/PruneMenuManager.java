package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import tim.prune.FunctionLibrary;
import tim.prune.I18nManager;
import tim.prune.PruneApp;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.RearrangeWaypointsFunction.Rearrange;
import tim.prune.function.browser.UrlGenerator;
import tim.prune.gui.map.PruneMapCanvas;
import tim.prune.gui.map.mgrs.MGRSOverlay;

/**
 * Class to manage the menu bar and tool bar,
 * including enabling and disabling the items
 */
public class PruneMenuManager extends MenuManager
{
	private PruneApp _app = null;
	private Track _track = null;
	private Selection _selection = null;

	// Menu items which need enabling/disabling
	private JMenuItem _sendGpsItem = null;
	private JMenuItem _saveItem = null;
	private JMenuItem _saveAsItem = null;
	private JMenuItem _saveSelectionItem = null;
	private JMenuItem _exportTextItem = null;
	private JMenuItem _exportKmlItem = null;
	private JMenuItem _exportGpxItem = null;
	private JMenuItem _undoItem = null;
	private JMenuItem _redoItem = null;
	private JMenuItem _clearUndoItem = null;
	private JMenuItem _editPointItem = null;
	private JMenuItem _editWaypointNameItem = null;
	private JMenuItem _deletePointItem = null;
	private JMenuItem _deleteRangeItem = null;
	private JMenuItem _compressItem = null;
	private JMenuItem _deleteMarkedPointsItem = null;
	private JMenuItem _interpolateItem = null;
	private JMenuItem _averageItem = null;
	private JMenuItem _selectAllItem = null;
	private JMenuItem _selectNoneItem = null;
	private JMenuItem _selectStartItem = null;
	private JMenuItem _selectEndItem = null;
	private JMenuItem _findWaypointItem = null;
	private JMenuItem _duplicatePointItem = null;
	private JMenuItem _reverseItem = null;
	private JMenuItem _addTimeOffsetItem = null;
	private JMenuItem _addAltitudeOffsetItem = null;
	private JMenuItem _mergeSegmentsItem = null;
	private JMenu     _rearrangeMenu = null;
	private JMenuItem _cutAndMoveItem = null;
	private JMenuItem _convertNamesToTimesItem = null;
	private JMenuItem _deleteFieldValuesItem = null;
	private JCheckBoxMenuItem _mapCheckbox = null;
	private JMenu     _browserMapMenu = null;
	private JMenuItem _chartItem = null;
	private JMenuItem _lookupSrtmItem = null;
	private JMenuItem _distanceItem = null;
	private JMenuItem _fullRangeDetailsItem = null;
	private JCheckBoxMenuItem _onlineCheckbox = null;

	// ActionListeners for reuse by menu and toolbar
	private ActionListener _openFileAction = null;
	private ActionListener _saveAction = null;
	private ActionListener _saveAsAction = null;
	private ActionListener _exportTextAction = null;
	private ActionListener _undoAction = null;
	private ActionListener _redoAction = null;
	private ActionListener _editPointAction = null;
	private ActionListener _deletePointAction = null;
	private ActionListener _deleteRangeAction = null;
	private ActionListener _selectStartAction = null;
	private ActionListener _selectEndAction = null;

	// Toolbar buttons which need enabling/disabling
	private JButton _saveButton = null;
	private JButton _undoButton = null;
	private JButton _redoButton = null;
	private JButton _editPointButton = null;
	private JButton _deletePointButton = null;
	private JButton _deleteRangeButton = null;
	private JButton _selectStartButton = null;
	private JButton _selectEndButton = null;


	/**
	 * Constructor
	 * @param inApp application to call on menu actions
	 * @param inTrackInfo track info object
	 */
	public PruneMenuManager(PruneApp inApp, TrackInfo inTrackInfo)
	{
		_app = inApp;
		_track = inTrackInfo.getTrack();
		_selection = inTrackInfo.getSelection();
	}


	/**
	 * Create a JMenuBar containing all menu items
	 * @return JMenuBar
	 */
	public JMenuBar createMenuBar()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu(I18nManager.getText("menu.file"));
		setAltKey(fileMenu, "altkey.menu.file");
		// Open file
		JMenuItem openMenuItem = new JMenuItem(I18nManager.getText("function.open"));
		setShortcut(openMenuItem, "shortcut.menu.file.open");
		_openFileAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.openFile();
			}
		};
		openMenuItem.addActionListener(_openFileAction);
		fileMenu.add(openMenuItem);
		fileMenu.addSeparator();
		// Load from GPS
		JMenuItem loadFromGpsMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSLOAD);
		setShortcut(loadFromGpsMenuItem, "shortcut.menu.file.load");
		fileMenu.add(loadFromGpsMenuItem);
		// Send to GPS
		_sendGpsItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSSAVE, false);
		fileMenu.add(_sendGpsItem);
		fileMenu.addSeparator();
		// Save
		_saveItem = new JMenuItem(I18nManager.getText("menu.file.save"), KeyEvent.VK_S);
		setShortcut(_saveItem, "shortcut.menu.file.save");
		_saveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveFile(false);
			}
		};
		_saveItem.addActionListener(_saveAction);
		_saveItem.setEnabled(false);
		fileMenu.add(_saveItem);
		// Save As...
		_saveAsItem = new JMenuItem(I18nManager.getText("menu.file.saveas"), null);
		_saveAsAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveFile(true);
			}
		};
		_saveAsItem.addActionListener(_saveAsAction);
		_saveAsItem.setEnabled(false);
		fileMenu.add(_saveAsItem);
		// Export - Kml
		_exportKmlItem = makeMenuItem(FunctionLibrary.FUNCTION_KMLEXPORT, false);
		fileMenu.add(_exportKmlItem);
		// Gpx
		_exportGpxItem = makeMenuItem(FunctionLibrary.FUNCTION_GPXEXPORT, false);
		fileMenu.add(_exportGpxItem);
		// Text
		_exportTextItem = new JMenuItem(I18nManager.getText("menu.file.exporttext"), null);
		_exportTextAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.exportAsText();
			}
		};
		_exportTextItem.addActionListener(_exportTextAction);
		_exportTextItem.setEnabled(false);
		fileMenu.add(_exportTextItem);
		fileMenu.addSeparator();
		JMenuItem exitMenuItem = new JMenuItem(I18nManager.getText("menu.file.exit"));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.exit();
			}
		});
		fileMenu.add(exitMenuItem);
		menubar.add(fileMenu);
		// Track menu
		JMenu trackMenu = new JMenu(I18nManager.getText("menu.track"));
		setAltKey(trackMenu, "altkey.menu.track");
		_undoItem = new JMenuItem(I18nManager.getText("menu.track.undo"));
		setShortcut(_undoItem, "shortcut.menu.track.undo");
		_undoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.undoActions(1);
			}
		};
		_undoItem.addActionListener(_undoAction);
		_undoItem.setEnabled(false);
		trackMenu.add(_undoItem);
		_redoItem = new JMenuItem(I18nManager.getText("menu.track.redo"));
		setShortcut(_redoItem, "shortcut.menu.track.redo");
		_redoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.redoActions(1);
			}
		};
		_redoItem.addActionListener(_redoAction);
		_redoItem.setEnabled(false);
		trackMenu.add(_redoItem);
		_clearUndoItem = new JMenuItem(I18nManager.getText("menu.track.clearundo"));
		_clearUndoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.clearUndo();
			}
		});
		_clearUndoItem.setEnabled(false);
		trackMenu.add(_clearUndoItem);
		trackMenu.addSeparator();
		_compressItem = makeMenuItem(FunctionLibrary.FUNCTION_COMPRESS, false);
		setShortcut(_compressItem, "shortcut.menu.edit.compress");
		trackMenu.add(_compressItem);
		_deleteMarkedPointsItem = new JMenuItem(I18nManager.getText("menu.track.deletemarked"));
		_deleteMarkedPointsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.finishCompressTrack();
			}
		});
		_deleteMarkedPointsItem.setEnabled(false);
		trackMenu.add(_deleteMarkedPointsItem);
		trackMenu.addSeparator();
		// Rearrange waypoints
		_rearrangeMenu = new JMenu(I18nManager.getText("menu.track.rearrange"));
		_rearrangeMenu.setEnabled(false);
		JMenuItem  rearrangeStartItem = new JMenuItem(I18nManager.getText("menu.track.rearrange.start"));
		rearrangeStartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_START);
			}
		});
		rearrangeStartItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeStartItem);
		JMenuItem rearrangeEndItem = new JMenuItem(I18nManager.getText("menu.track.rearrange.end"));
		rearrangeEndItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_END);
			}
		});
		rearrangeEndItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeEndItem);
		JMenuItem rearrangeNearestItem = new JMenuItem(I18nManager.getText("menu.track.rearrange.nearest"));
		rearrangeNearestItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_NEAREST);
			}
		});
		rearrangeNearestItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeNearestItem);
		trackMenu.add(_rearrangeMenu);
		_lookupSrtmItem = makeMenuItem(FunctionLibrary.FUNCTION_LOOKUP_SRTM, false);
		trackMenu.add(_lookupSrtmItem);
		menubar.add(trackMenu);

		// Range menu
		JMenu rangeMenu = new JMenu(I18nManager.getText("menu.range"));
		setAltKey(rangeMenu, "altkey.menu.range");
		_selectAllItem = new JMenuItem(I18nManager.getText("menu.range.all"));
		setShortcut(_selectAllItem, "shortcut.menu.range.all");
		_selectAllItem.setEnabled(false);
		_selectAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRange(0, _track.getNumPoints()-1);
			}
		});
		rangeMenu.add(_selectAllItem);
		_selectNoneItem = new JMenuItem(I18nManager.getText("menu.range.none"));
		_selectNoneItem.setEnabled(false);
		_selectNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.selectNone();
			}
		});
		rangeMenu.add(_selectNoneItem);
		rangeMenu.addSeparator();
		_selectStartItem = new JMenuItem(I18nManager.getText("menu.range.start"));
		_selectStartItem.setEnabled(false);
		_selectStartAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRangeStart();
			}
		};
		_selectStartItem.addActionListener(_selectStartAction);
		rangeMenu.add(_selectStartItem);
		_selectEndItem = new JMenuItem(I18nManager.getText("menu.range.end"));
		_selectEndItem.setEnabled(false);
		_selectEndAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRangeEnd();
			}
		};
		_selectEndItem.addActionListener(_selectEndAction);
		rangeMenu.add(_selectEndItem);
		rangeMenu.addSeparator();
		_deleteRangeItem = new JMenuItem(I18nManager.getText("menu.range.deleterange"));
		_deleteRangeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.deleteSelectedRange();
			}
		};
		_deleteRangeItem.addActionListener(_deleteRangeAction);
		_deleteRangeItem.setEnabled(false);
		_deleteRangeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		rangeMenu.add(_deleteRangeItem);
		_saveSelectionItem = new JMenuItem(I18nManager.getText("menu.range.save"));
		_saveSelectionItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveSelectionAs();
			}
		});
		_saveSelectionItem.setEnabled(false);
		rangeMenu.add(_saveSelectionItem);
		_reverseItem = new JMenuItem(I18nManager.getText("menu.range.reverse"));
		_reverseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.reverseRange();
			}
		});
		_reverseItem.setEnabled(false);
		rangeMenu.add(_reverseItem);
		_addTimeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_TIME_OFFSET, false);
		rangeMenu.add(_addTimeOffsetItem);
		_addAltitudeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_ALTITUDE_OFFSET, false);
		rangeMenu.add(_addAltitudeOffsetItem);
		_mergeSegmentsItem = new JMenuItem(I18nManager.getText("menu.range.mergetracksegments"));
		_mergeSegmentsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.mergeTrackSegments();
			}
		});
		_mergeSegmentsItem.setEnabled(false);
		rangeMenu.add(_mergeSegmentsItem);
		_deleteFieldValuesItem = makeMenuItem(FunctionLibrary.FUNCTION_DELETE_FIELD_VALUES, false);
		rangeMenu.add(_deleteFieldValuesItem);
		rangeMenu.addSeparator();
		_interpolateItem = new JMenuItem(I18nManager.getText("menu.range.interpolate"));
		_interpolateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.interpolateSelection();
			}
		});
		_interpolateItem.setEnabled(false);
		rangeMenu.add(_interpolateItem);
		_averageItem = new JMenuItem(I18nManager.getText("menu.range.average"));
		_averageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.averageSelection();
			}
		});
		_averageItem.setEnabled(false);
		rangeMenu.add(_averageItem);
		_cutAndMoveItem = new JMenuItem(I18nManager.getText("menu.range.cutandmove"));
		_cutAndMoveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.cutAndMoveSelection();
			}
		});
		_cutAndMoveItem.setEnabled(false);
		rangeMenu.add(_cutAndMoveItem);
		_convertNamesToTimesItem = makeMenuItem(FunctionLibrary.FUNCTION_CONVERT_NAMES_TO_TIMES, false);
		rangeMenu.add(_convertNamesToTimesItem);
		menubar.add(rangeMenu);

		// Point menu
		JMenu pointMenu = new JMenu(I18nManager.getText("menu.point"));
		setAltKey(pointMenu, "altkey.menu.point");
		_editPointItem = new JMenuItem(I18nManager.getText("menu.point.editpoint"));
		_editPointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.editCurrentPoint();
			}
		};
		_editPointItem.addActionListener(_editPointAction);
		_editPointItem.setEnabled(false);
		pointMenu.add(_editPointItem);
		_editWaypointNameItem = makeMenuItem(FunctionLibrary.FUNCTION_EDIT_WAYPOINT_NAME, false);
		pointMenu.add(_editWaypointNameItem);
		_deletePointItem = new JMenuItem(I18nManager.getText("menu.point.deletepoint"));
		_deletePointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.deleteCurrentPoint();
			}
		};
		_deletePointItem.addActionListener(_deletePointAction);
		_deletePointItem.setEnabled(false);
		_deletePointItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		pointMenu.add(_deletePointItem);
		pointMenu.addSeparator();
		// find a waypoint
		_findWaypointItem = makeMenuItem(FunctionLibrary.FUNCTION_FIND_WAYPOINT, false);
		pointMenu.add(_findWaypointItem);
		// duplicate current point
		_duplicatePointItem = makeMenuItem(FunctionLibrary.FUNCTION_DUPLICATE_POINT, false);
		pointMenu.add(_duplicatePointItem);
		// paste coordinates function
		JMenuItem pasteCoordsItem = makeMenuItem(FunctionLibrary.FUNCTION_PASTE_COORDINATES);
		pointMenu.add(pasteCoordsItem);
		menubar.add(pointMenu);

		// Add view menu
		JMenu viewMenu = new JMenu(I18nManager.getText("menu.view"));
		setAltKey(viewMenu, "altkey.menu.view");
		// Turn map display on/off
		_mapCheckbox = new JCheckBoxMenuItem(
			I18nManager.getText("menu.map.showmap"), false);
		_mapCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.setConfigBoolean(Config.KEY_SHOW_MAP, _mapCheckbox.isSelected());
				UpdateMessageBroker.informSubscribers();
			}
		});
		viewMenu.add(_mapCheckbox);
		// Turn off the sidebars
		JCheckBoxMenuItem sidebarsCheckbox = new JCheckBoxMenuItem(I18nManager.getText("menu.view.showsidebars"));
		sidebarsCheckbox.setSelected(true);
		sidebarsCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.toggleSidebars();
			}
		});
		viewMenu.add(sidebarsCheckbox);
		// browser submenu
		_browserMapMenu = new JMenu(I18nManager.getText("menu.view.browser"));
		_browserMapMenu.setEnabled(false);
		JMenuItem googleMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.google"));
		googleMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_GOOGLE);
			}
		});
		_browserMapMenu.add(googleMapsItem);
		JMenuItem openMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.openstreetmap"));
		openMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_OSM);
			}
		});
		_browserMapMenu.add(openMapsItem);
		JMenuItem mapquestMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.mapquest"));
		mapquestMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_MAPQUEST);
			}
		});
		_browserMapMenu.add(mapquestMapsItem);
		JMenuItem yahooMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.yahoo"));
		yahooMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_YAHOO);
			}
		});
		_browserMapMenu.add(yahooMapsItem);
		JMenuItem bingMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.bing"));
		bingMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_BING);
			}
		});
		_browserMapMenu.add(bingMapsItem);
		viewMenu.add(_browserMapMenu);
		// Charts
		_chartItem = makeMenuItem(FunctionLibrary.FUNCTION_CHARTS, false);
		viewMenu.add(_chartItem);
		// Distances
		_distanceItem = makeMenuItem(FunctionLibrary.FUNCTION_DISTANCES, false);
		viewMenu.add(_distanceItem);
		// full range details
		_fullRangeDetailsItem = makeMenuItem(FunctionLibrary.FUNCTION_FULL_RANGE_DETAILS, false);
		viewMenu.add(_fullRangeDetailsItem);
		menubar.add(viewMenu);

		// Settings menu
		JMenu settingsMenu = new JMenu(I18nManager.getText("menu.settings"));
		setAltKey(settingsMenu, "altkey.menu.settings");
		// Set the map background
		JMenuItem mapBgItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_MAP_BG);
		settingsMenu.add(mapBgItem);
		_onlineCheckbox = new JCheckBoxMenuItem(I18nManager.getText("menu.settings.onlinemode"));
		_onlineCheckbox.setSelected(Config.getConfigBoolean(Config.KEY_ONLINE_MODE));
		_onlineCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isOnline = _onlineCheckbox.isSelected();
				Config.setConfigBoolean(Config.KEY_ONLINE_MODE, isOnline);
				if (isOnline) {UpdateMessageBroker.informSubscribers();}
			}
		});
		settingsMenu.add(_onlineCheckbox);
		final JMenuItem showMapSourceOption = new JCheckBoxMenuItem(
				I18nManager.getText("menu.settings.showtilesource"));
		showMapSourceOption.setSelected(Config.getConfigBoolean(Config.KEY_SHOW_TILE_SOURCE));
		showMapSourceOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showMapSource = showMapSourceOption.isSelected();
				Config.setConfigBoolean(Config.KEY_SHOW_TILE_SOURCE, showMapSource);
				UpdateMessageBroker.informSubscribers();
			}
		});
		settingsMenu.add(showMapSourceOption);

		final JMenuItem showMGRSGridOption = new JCheckBoxMenuItem(
				I18nManager.getText("menu.settings.showmgrsgrid"));
		showMGRSGridOption.setSelected(Config.getConfigBoolean(Config.KEY_SHOW_MGRS_GRID));
		showMGRSGridOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showGrid = showMGRSGridOption.isSelected();
				if (!showGrid) {
					_app.getOverlays().removeOverlay(MGRSOverlay.getInstance());
				} else {
					_app.getOverlays().addOverlay(MGRSOverlay.getInstance());
				}
				Config.setConfigBoolean(Config.KEY_SHOW_MGRS_GRID, showGrid);
				_app.getCanvas().repaint();
			}
		});
		settingsMenu.add(showMGRSGridOption);

//		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_DISK_CACHE));
		settingsMenu.addSeparator();
		// Set kmz image size
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_KMZ_IMAGE_SIZE));
		// Set program paths
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_PATHS));
		// Set colours
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_COLOURS));
		// Set line width used for drawing
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_LINE_WIDTH));
		// Set language
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_LANGUAGE));
//		settingsMenu.addSeparator();
//		// Save configuration
//		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SAVECONFIG));
		menubar.add(settingsMenu);

		// Help menu
		JMenu helpMenu = new JMenu(I18nManager.getText("menu.help"));
		setAltKey(helpMenu, "altkey.menu.help");
		JMenuItem helpItem = makeMenuItem(FunctionLibrary.FUNCTION_HELP);
		setShortcut(helpItem, "shortcut.menu.help.help");
		helpMenu.add(helpItem);
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SHOW_KEYS));
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_ABOUT));
		menubar.add(helpMenu);

		return menubar;
	}

	/**
	 * Create a JToolBar containing all toolbar buttons
	 * @return toolbar containing buttons
	 */
	public JToolBar createToolBar()
	{
		JToolBar toolbar = new JToolBar();
		// Add text file
		JButton openFileButton = new JButton(IconManager.getImageIcon(IconManager.OPEN_FILE));
		openFileButton.setToolTipText(I18nManager.getText("function.open"));
		openFileButton.addActionListener(_openFileAction);
		toolbar.add(openFileButton);
		// Save
		_saveButton = new JButton(IconManager.getImageIcon(IconManager.SAVE_FILE));
		_saveButton.setToolTipText(I18nManager.getText("menu.file.save"));
		_saveButton.addActionListener(_saveAction);
		_saveButton.setEnabled(false);
		toolbar.add(_saveButton);
		// Undo
		_undoButton = new JButton(IconManager.getImageIcon(IconManager.UNDO));
		_undoButton.setToolTipText(I18nManager.getText("menu.track.undo"));
		_undoButton.addActionListener(_undoAction);
		_undoButton.setEnabled(false);
		toolbar.add(_undoButton);
		// Redo
		_redoButton = new JButton(IconManager.getImageIcon(IconManager.REDO));
		_redoButton.setToolTipText(I18nManager.getText("menu.track.redo"));
		_redoButton.addActionListener(_redoAction);
		_redoButton.setEnabled(false);
		toolbar.add(_redoButton);
		// Edit point
		_editPointButton = new JButton(IconManager.getImageIcon(IconManager.EDIT_POINT));
		_editPointButton.setToolTipText(I18nManager.getText("menu.point.editpoint"));
		_editPointButton.addActionListener(_editPointAction);
		_editPointButton.setEnabled(false);
		toolbar.add(_editPointButton);
		// Delete point
		_deletePointButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_POINT));
		_deletePointButton.setToolTipText(I18nManager.getText("menu.point.deletepoint"));
		_deletePointButton.addActionListener(_deletePointAction);
		_deletePointButton.setEnabled(false);
		toolbar.add(_deletePointButton);
		// Delete range
		_deleteRangeButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_RANGE));
		_deleteRangeButton.setToolTipText(I18nManager.getText("menu.range.deleterange"));
		_deleteRangeButton.addActionListener(_deleteRangeAction);
		_deleteRangeButton.setEnabled(false);
		toolbar.add(_deleteRangeButton);
		// Select start, end
		_selectStartButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_START));
		_selectStartButton.setToolTipText(I18nManager.getText("menu.range.start"));
		_selectStartButton.addActionListener(_selectStartAction);
		_selectStartButton.setEnabled(false);
		toolbar.add(_selectStartButton);
		_selectEndButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_END));
		_selectEndButton.setToolTipText(I18nManager.getText("menu.range.end"));
		_selectEndButton.addActionListener(_selectEndAction);
		_selectEndButton.setEnabled(false);
		toolbar.add(_selectEndButton);
		// finish off
		toolbar.setFloatable(false);
		return toolbar;
	}


	/**
	 * Method to update menu when file loaded
	 */
	public void informFileLoaded()
	{
		// save, undo, delete enabled
		_sendGpsItem.setEnabled(true);
		_saveItem.setEnabled(true);
		_saveAsItem.setEnabled(true);
		_exportTextItem.setEnabled(true);
		_undoItem.setEnabled(true);
		_redoItem.setEnabled(true);
		_compressItem.setEnabled(true);
		_deleteMarkedPointsItem.setEnabled(false);
	}


	/**
	 * @see tim.prune.DataSubscriber#dataUpdated(tim.prune.data.Track)
	 */
	public void dataUpdated(byte inUpdateType)
	{
		boolean hasData = (_track != null && _track.getNumPoints() > 0);
		// set functions which require data
		_sendGpsItem.setEnabled(hasData);
		_saveItem.setEnabled(hasData);
		_saveAsItem.setEnabled(hasData);
		_exportTextItem.setEnabled(hasData);
		_saveButton.setEnabled(hasData);
		_exportKmlItem.setEnabled(hasData);
		_exportGpxItem.setEnabled(hasData);
		_compressItem.setEnabled(hasData);
		_deleteMarkedPointsItem.setEnabled(hasData && _track.hasMarkedPoints());
		_rearrangeMenu.setEnabled(hasData && _track.hasTrackPoints() && _track.hasWaypoints());
		_selectAllItem.setEnabled(hasData);
		_selectNoneItem.setEnabled(hasData);
		_chartItem.setEnabled(hasData);
		_browserMapMenu.setEnabled(hasData);
		_distanceItem.setEnabled(hasData);
		_lookupSrtmItem.setEnabled(hasData);
		_findWaypointItem.setEnabled(hasData && _track.hasWaypoints());
		// is undo available?
		boolean hasUndo = !_app.getUndoStack().isEmpty();
		_undoItem.setEnabled(hasUndo);
		_undoButton.setEnabled(hasUndo);
		boolean hasRedo = !_app.getRedoStack().isEmpty();
		_redoItem.setEnabled(hasRedo);
		_redoButton.setEnabled(hasRedo);
		_clearUndoItem.setEnabled(hasUndo || hasRedo);
		// is there a current point?
		boolean hasPoint = (hasData && _selection.getCurrentPointIndex() >= 0);
		_editPointItem.setEnabled(hasPoint);
		_editPointButton.setEnabled(hasPoint);
		_editWaypointNameItem.setEnabled(hasPoint);
		_deletePointItem.setEnabled(hasPoint);
		_deletePointButton.setEnabled(hasPoint);
		_selectStartItem.setEnabled(hasPoint);
		_selectStartButton.setEnabled(hasPoint);
		_selectEndItem.setEnabled(hasPoint);
		_selectEndButton.setEnabled(hasPoint);
		_duplicatePointItem.setEnabled(hasPoint);
		// is there a current range?
		boolean hasRange = (hasData && _selection.hasRangeSelected());
		_saveSelectionItem.setEnabled(hasRange);
		((PruneMapCanvas)_app.getCanvas()).setSaveRangeEnabled(hasRange);
		_deleteRangeItem.setEnabled(hasRange);
		_deleteRangeButton.setEnabled(hasRange);
		_interpolateItem.setEnabled(hasRange
			&& (_selection.getEnd() - _selection.getStart()) == 1);
		_averageItem.setEnabled(hasRange);
		_mergeSegmentsItem.setEnabled(hasRange);
		_reverseItem.setEnabled(hasRange);
		_addTimeOffsetItem.setEnabled(hasRange);
		_addAltitudeOffsetItem.setEnabled(hasRange);
		_convertNamesToTimesItem.setEnabled(hasRange && _track.hasWaypoints());
		_deleteFieldValuesItem.setEnabled(hasRange);
		_fullRangeDetailsItem.setEnabled(hasRange);
		// Is the currently selected point outside the current range?
		_cutAndMoveItem.setEnabled(hasRange && hasPoint &&
			(_selection.getCurrentPointIndex() < _selection.getStart()
				|| _selection.getCurrentPointIndex() > (_selection.getEnd()+1)));
		// Has the map been switched on/off?
		boolean mapsOn = Config.getConfigBoolean(Config.KEY_SHOW_MAP);
		if (_mapCheckbox.isSelected() != mapsOn) {
			_mapCheckbox.setSelected(mapsOn);
		}
	}


	/**
	 * Ignore action completed signals
	 * @see tim.prune.DataSubscriber#actionCompleted(java.lang.String)
	 */
	public void actionCompleted(String inMessage)
	{}
}
