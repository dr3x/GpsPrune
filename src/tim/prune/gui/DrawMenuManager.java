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

import tim.prune.DrawApp;
import tim.prune.FunctionLibrary;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.drawing.DrawingItem;
import tim.prune.gui.map.mgrs.MGRSOverlay;

public class DrawMenuManager extends MenuManager {

	private DrawApp _app = null;

	// Menu items which need enabling/disabling
	private JMenuItem _saveItem = null;
	private JMenuItem _undoItem = null;
	private JMenuItem _redoItem = null;
	private JMenuItem _clearUndoItem = null;
	private JMenuItem _deletePointItem = null;
	private JMenuItem _copyItem = null;
	private JMenuItem _pasteItem = null;
	private JCheckBoxMenuItem _mapCheckbox = null;
	private JCheckBoxMenuItem _onlineCheckbox = null;
	
	// ActionListeners for reuse by menu and toolbar
	private ActionListener _openFileAction = null;
	private ActionListener _saveAction = null;
	private ActionListener _undoAction = null;
	private ActionListener _redoAction = null;
	private ActionListener _deletePointAction = null;
	private ActionListener _copyPasteAction = null;
	
	// Toolbar buttons which need enabling/disabling
	private JButton _saveButton = null;
	private JButton _undoButton = null;
	private JButton _redoButton = null;
	private JButton _deletePointButton = null;

	private JMenuItem _saveAsMenuItem;


	/**
	 * Constructor
	 * @param inApp application to call on menu actions
	 */
	public DrawMenuManager(DrawApp inApp)
	{
		_app = inApp;
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

		
		// New file
		JMenuItem newMenuItem = new JMenuItem(I18nManager.getText("New"));
		setShortcut(newMenuItem, "shortcut.menu.file.new");
		newMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.newFile();
			}
		});
		fileMenu.add(newMenuItem);
		
		
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
		
		// Append file
		JMenuItem appendMenuItem = new JMenuItem(I18nManager.getText("Append"));
		setShortcut(appendMenuItem, "shortcut.menu.file.append");
		appendMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.appendFile();
			}
		});
		fileMenu.add(appendMenuItem);
		
		
		// Save file
		_saveItem = new JMenuItem(I18nManager.getText("Save"), KeyEvent.VK_S);
		setShortcut(_saveItem, "shortcut.menu.file.save");
		_saveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveFile(false);
			}
		};
		_saveItem.addActionListener(_saveAction);
		_saveItem.setEnabled(false);
		fileMenu.add(_saveItem);
		
		_saveAsMenuItem = new JMenuItem(I18nManager.getText("Save As..."));
		_saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveFile(true);
			}
		});
		_saveAsMenuItem.setEnabled(false);
		fileMenu.add(_saveAsMenuItem);
		fileMenu.addSeparator();
		
		// Exit
		JMenuItem exitMenuItem = new JMenuItem(I18nManager.getText("menu.file.exit"));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.exit();
			}
		});
		fileMenu.add(exitMenuItem);
		menubar.add(fileMenu);
		
		
		// edit menu
		JMenu trackMenu = new JMenu(I18nManager.getText("menu.edit"));
		setAltKey(trackMenu, "altkey.menu.edit");
		_copyItem = new JMenuItem(I18nManager.getText("menu.edit.copy"));
		_copyItem.setActionCommand("copy");
		setShortcut(_copyItem, "shortcut.menu.edit.copy");
		_copyPasteAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( "copy".equals(e.getActionCommand()) ) {
					_pasteItem.setEnabled(_app.copySelected());
				} else {
					_app.pasteSelected();
				}
			}
		};
		_copyItem.addActionListener(_copyPasteAction);
		_pasteItem = new JMenuItem(I18nManager.getText("menu.edit.paste"));
		_pasteItem.setActionCommand("paste");
		setShortcut(_pasteItem, "shortcut.menu.edit.paste");
		_pasteItem.addActionListener(_copyPasteAction);
		_copyItem.setEnabled(false);
		_pasteItem.setEnabled(false);
		trackMenu.add(_copyItem);
		trackMenu.add(_pasteItem);
		
		trackMenu.addSeparator();
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

		// Point menu
		_deletePointItem = new JMenuItem(I18nManager.getText("Delete"));
		_deletePointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.deleteSelected();
			}
		};
		_deletePointItem.addActionListener(_deletePointAction);
		_deletePointItem.setEnabled(false);
		_deletePointItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		trackMenu.add(_deletePointItem);
		menubar.add(trackMenu);

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

		menubar.add(settingsMenu);
		
		// Help menu
		JMenu helpMenu = new JMenu(I18nManager.getText("menu.help"));
		setAltKey(helpMenu, "altkey.menu.help");
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SHOW_KEYS));
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_ABOUT));
		menubar.add(helpMenu);

		_app.getSelectionListeners().add(new SelectionListener());
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
		JButton openFileButton = new JButton(IconManager.getImageIcon(IconManager.DRAW_OPEN_FILE));
		openFileButton.setToolTipText(I18nManager.getText("function.open"));
		openFileButton.addActionListener(_openFileAction);
		openFileButton.setBorderPainted(false);
		toolbar.add(openFileButton);
		
		// Save
		_saveButton = new JButton(IconManager.getImageIcon(IconManager.DRAW_SAVE_FILE));
		_saveButton.setToolTipText(I18nManager.getText("Save"));
		_saveButton.addActionListener(_saveAction);
		_saveButton.setEnabled(false);
		_saveButton.setBorderPainted(false);
		toolbar.add(_saveButton);
		
		toolbar.addSeparator();
		
		// Undo
		_undoButton = new JButton(IconManager.getImageIcon(IconManager.UNDO));
		_undoButton.setToolTipText(I18nManager.getText("menu.track.undo"));
		_undoButton.addActionListener(_undoAction);
		_undoButton.setEnabled(false);
		_undoButton.setBorderPainted(false);
		toolbar.add(_undoButton);
		
		// Redo
		_redoButton = new JButton(IconManager.getImageIcon(IconManager.REDO));
		_redoButton.setToolTipText(I18nManager.getText("menu.track.redo"));
		_redoButton.addActionListener(_redoAction);
		_redoButton.setEnabled(false);
		_redoButton.setBorderPainted(false);
		toolbar.add(_redoButton);
		
		// Delete point
		_deletePointButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_POINT));
		_deletePointButton.setToolTipText(I18nManager.getText("Delete"));
		_deletePointButton.addActionListener(_deletePointAction);
		_deletePointButton.setEnabled(false);
		_deletePointButton.setBorderPainted(false);
		toolbar.add(_deletePointButton);
		
		return toolbar;
	}


	/**
	 * Method to update menu when file loaded
	 */
	public void informFileLoaded()
	{
		// save, undo, delete enabled
		_saveItem.setEnabled(true);
		_undoItem.setEnabled(true);
		_redoItem.setEnabled(true);
		_saveAsMenuItem.setEnabled(true);
	}


	/**
	 * @see tim.prune.DataSubscriber#dataUpdated(tim.prune.data.Track)
	 */
	public void dataUpdated(byte inUpdateType)
	{
		boolean hasData = true;
		// set functions which require data
		_saveItem.setEnabled(hasData);
		_saveButton.setEnabled(hasData);
		_saveAsMenuItem.setEnabled(hasData);
		// is undo available?
		boolean hasUndo = !_app.getUndoStack().isEmpty();
		_undoItem.setEnabled(hasUndo);
		_undoButton.setEnabled(hasUndo);
		boolean hasRedo = !_app.getRedoStack().isEmpty();
		_redoItem.setEnabled(hasRedo);
		_redoButton.setEnabled(hasRedo);
		_clearUndoItem.setEnabled(hasUndo || hasRedo);
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
	
	private final class SelectionListener implements DrawApp.SelectionListener {
		@Override
		public void onSelected(DrawingItem item) {
			_copyItem.setEnabled(item != null);
			_deletePointItem.setEnabled(item != null);
			_deletePointButton.setEnabled(item != null);
		}
	}
}
