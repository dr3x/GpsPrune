package tim.prune;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.config.Config;
import tim.prune.data.FileInfo;
import tim.prune.gui.MenuManager;
import tim.prune.gui.SidebarController;
import tim.prune.gui.Viewport;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.load.FileLoader;
import tim.prune.overlay.OverlayManager;
import tim.prune.undo.UndoException;
import tim.prune.undo.UndoOperation;
import tim.prune.waypoint.GotoManager;


/**
 * Main controller for the application
 */
public abstract class App implements FileLoadedInterface
{
	// Instance variables
	private File _baseDir;
	private OverlayManager overlays;
	private JFrame _frame = null;	
	private MenuManager _menuManager = null;
	private SidebarController _sidebarController = null;
	private Stack<UndoOperation> _undoStack = null;
	private Stack<UndoOperation> _redoStack = null;
	protected FileLoader _fileLoader = null;
	protected ArrayList<File> _dataFiles = null;
	private Viewport _viewport = null;
	private boolean _firstDataFile = true;
	private boolean _undoEnabled = true;
	private boolean _inRedo = false;
	private String _title = "'";
	private File _openFile = null;


	private void configLogging() {
		String logFile = Config.getConfigString(Config.KEY_BASE_SUPPORT_LOCATION)+"/"+this.getClass().getName()+".log";
		try {
			Handler fh = new FileHandler(logFile+".xml", 1024*1024, 3);
			Logger.getLogger("").addHandler(fh);
			fh = new FileHandler(logFile, 1024*1024, 3);
			fh.setFormatter(new SimpleFormatter());
			Logger.getLogger("").addHandler(fh);
			Logger.getLogger("").setLevel(Level.INFO);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * @param inFrame frame object for application
	 */
	@SuppressWarnings("serial")
	public App(JFrame inFrame, File baseDir)
	{
		configLogging();
		_frame = inFrame;
		_title = inFrame.getTitle() == null?"":inFrame.getTitle();
		_baseDir = baseDir;
		_undoStack = new Stack<UndoOperation>() {
			public synchronized boolean add(UndoOperation obj) {
				if( _undoEnabled ) {
					if (!_inRedo) _redoStack.clear();
					return super.add(obj);
				}
				return false;
			}
			public synchronized void addElement(UndoOperation obj) {
				if( _undoEnabled ) {
					if (!_inRedo) _redoStack.clear();
					super.addElement(obj);
				}
			}
		};
		_redoStack = new Stack<UndoOperation>() {
			public synchronized boolean add(UndoOperation obj) {
				if( _undoEnabled ) {
					return super.add(obj);
				}
				return false;
			}
			public synchronized void addElement(UndoOperation obj) {
				if( _undoEnabled ) {
					super.addElement(obj);
				}
			}
		};
		overlays = new OverlayManager();
		_frame.setSize(Config.getConfigInt(Config.KEY_LAST_SCREEN_WIDTH), Config.getConfigInt(Config.KEY_LAST_SCREEN_HEIGHT));
	}

	public MapCanvas getCanvas() {
		return _viewport.getMapCanvas();
	}

	public OverlayManager getOverlays() {
		return overlays;
	}

	/**
	 * @return the dialog frame
	 */
	public JFrame getFrame()
	{
		return _frame;
	}
	
	public File getBaseDir() {
		return getBaseDir(false);
	}
	
	public File getBaseDir(boolean create) {
		if( !create && !_baseDir.exists() )
			_baseDir.mkdirs();
		return _baseDir;
	}

	public MenuManager getMenuManager() {
		return _menuManager;
	}

	public void setFirstDataFile(boolean _firstDataFile) {
		this._firstDataFile = _firstDataFile;
	}

	public boolean isFirstDataFile() {
		return _firstDataFile;
	}

	public void setDataFiles(ArrayList<File> _dataFiles) {
		this._dataFiles = _dataFiles;
	}

	public ArrayList<File> getDataFiles() {
		return _dataFiles;
	}

	public void setUndoEnabled(boolean _undoEnabled) {
		this._undoEnabled = _undoEnabled;
	}

	public boolean isUndoEnabled() {
		return _undoEnabled;
	}

	/**
	 * Check if the application has unsaved data
	 * @return true if data is unsaved, false otherwise
	 */
	public abstract boolean hasDataUnsaved();

	/**
	 * Open a file containing track or waypoint data
	 */
	public void openFile()
	{
		openFile(false);
	}
	public void openFile(boolean multiple)
	{
		if (_fileLoader == null) {
			_fileLoader = new FileLoader(this, getFrame());
			_fileLoader.setBaseDir(_baseDir);
		}
		_fileLoader.openFile(multiple);
	}

	public abstract void saveFile(boolean saveAs);

	public void onFileLoaded( FileInfo info ) {
		if( getDataFiles() == null || getDataFiles().isEmpty() ) {
			loadComplete();			
		}
	}

	/**
	 * @return the undo stack
	 */
	public Stack<UndoOperation> getUndoStack()
	{
		return _undoStack;
	}

	/**
	 * @return the redo stack
	 */
	public Stack<UndoOperation> getRedoStack()
	{
		return _redoStack;
	}

	/**
	 * Load the specified data files one by one
	 * @param inDataFiles arraylist containing File objects to load
	 */
	public void loadDataFiles(ArrayList<File> inDataFiles) {
		setUndoEnabled(false);
		if (inDataFiles == null || inDataFiles.size() < 1 ) {
			setDataFiles(null);
		}
		else {
			inDataFiles = new ArrayList<File>(inDataFiles);
			_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			updateTitle(inDataFiles);
			setDataFiles(inDataFiles);
			
			if( inDataFiles.size() == 1 ) {
				_openFile = inDataFiles.get(0);
			} else {
				_openFile = null;
			}
			
			File f = getDataFiles().remove(0);
			// Start load of specified file
			if (_fileLoader == null)
				_fileLoader = new FileLoader(this, getFrame());
			setFirstDataFile(true);
			_fileLoader.openFile(f);
		}
		setUndoEnabled(true);
	}

	protected void updateTitle( List<File> files ) {
		StringBuilder fileNames = new StringBuilder();
		for( File f : files ) {
			if( fileNames.length() > 0 ) fileNames.append(", ");
			fileNames.append(f.getName());
		}
		_frame.setTitle(_title+" ["+fileNames+"]");
	}
	
	protected void updateTitle() {
		_frame.setTitle(_title+" [Untitled]");
	}

	/**
	 * Load the next file in the waiting list, if any
	 */
	protected void loadNextFile()
	{
		setFirstDataFile(false);
		if (getDataFiles() == null || getDataFiles().size() == 0) {
			setDataFiles(null);
		}
		else {
			new Thread(new Runnable() {
				public void run() {
					File f = getDataFiles().remove(0);
					setUndoEnabled(false);
					_fileLoader.openFile(f);
					setUndoEnabled(true);
				}
			}).start();
		}
	}
	
	protected void loadComplete() {
		_frame.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Complete a function execution
	 * @param inUndo undo object to be added to stack
	 * @param inConfirmText confirmation text
	 */
	public void completeFunction(UndoOperation inUndo, String inConfirmText)
	{
		if (!_inRedo && (inUndo != null)) _undoStack.add(inUndo);
		UpdateMessageBroker.informSubscribers(inConfirmText);
	}

	/**
	 * Set the MenuManager object to be informed about changes
	 * @param inManager MenuManager object
	 */
	public void setMenuManager(MenuManager inManager)
	{
		_menuManager = inManager;
	}


	/**
	 * Exit the application if confirmed
	 */
	public void exit()
	{
		// grab focus
		_frame.toFront();
		_frame.requestFocus();
		// check if ok to exit
		if (confirmDataLoss("exiting"))
		{
			Config.setConfigInt(Config.KEY_LAST_SCREEN_WIDTH, _frame.getWidth());
			Config.setConfigInt(Config.KEY_LAST_SCREEN_HEIGHT, _frame.getHeight());
			GotoManager.getInstance().saveLastPoint();
			MapSourceLibrary.closeCaches();
			System.exit(0);
		}
	}
	
	protected boolean confirmDataLoss(String action) {
		if( hasDataUnsaved() ) {
			String[] options = new String[] {"Save Changes First", "Abandon Changes"};
			JOptionPane confirm = new JOptionPane(
					"Would you like to save before " + action + "?  If you don't save, your changes will be lost.", 
					JOptionPane.QUESTION_MESSAGE);
			confirm.setOptions(options);
			JDialog dialog = confirm.createDialog(getFrame(), "Changes Pending");
			dialog.setVisible(true);
			Object selectedValue = confirm.getValue();

			if( options[0].equals(selectedValue) ) {
				saveFile(false);
			}
		}
		return true;
	}


	/**
	 * Clear the undo stack (losing all undo information
	 */
	public void clearUndo()
	{
		// Exit if nothing to undo
		if ((_undoStack == null || _undoStack.isEmpty()) && 
				(_redoStack == null || _redoStack.isEmpty()))
			return;
		// Confirm operation with dialog
		int answer = JOptionPane.showConfirmDialog(_frame,
			I18nManager.getText("dialog.clearundo.text"),
			I18nManager.getText("dialog.clearundo.title"),
			JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION)
		{
			if (_undoStack != null) _undoStack.clear();
			if (_redoStack != null) _redoStack.clear();
			UpdateMessageBroker.informSubscribers();
		}
	}


	/**
	 * Undo the specified number of actions
	 * @param inNumUndos number of actions to undo
	 */
	public void undoActions(int inNumUndos)
	{
		try
		{
			for (int i=0; i<inNumUndos; i++)
			{
				UndoOperation op = _undoStack.pop();
				_redoStack.add(op);
				op.performUndo(this);
			}
			String message = "" + inNumUndos + " "
				 + (inNumUndos==1?I18nManager.getText("confirm.undo.single"):I18nManager.getText("confirm.undo.multi"));
			UpdateMessageBroker.informSubscribers(message);
		}
		catch (UndoException ue)
		{
			showErrorMessageNoLookup("error.undofailed.title",
				I18nManager.getText("error.undofailed.text") + " : " + ue.getMessage());
			_undoStack.clear();
			_redoStack.clear();
			UpdateMessageBroker.informSubscribers();
		}
		catch (EmptyStackException empty) {}
	}

	/**
	 * Redo the specified number of actions
	 * @param inNumUndos number of actions to redo
	 */
	public void redoActions(int inNumRedos)
	{
		try
		{
			_inRedo = true;
			try {
				for (int i=0; i<inNumRedos; i++)
				{
					UndoOperation op = _redoStack.pop();
					_undoStack.add(op);
					op.performRedo(this);
				}
			} finally {
				_inRedo = false;
			}
			String message = "" + inNumRedos + " "
				 + (inNumRedos==1?I18nManager.getText("confirm.redo.single"):I18nManager.getText("confirm.redo.multi"));
			UpdateMessageBroker.informSubscribers(message);
		}
		catch (UndoException ue)
		{
			showErrorMessageNoLookup("error.redofailed.title",
				I18nManager.getText("error.redofailed.text") + " : " + ue.getMessage());
			_undoStack.clear();
			_redoStack.clear();
			UpdateMessageBroker.informSubscribers();
		}
		catch (EmptyStackException empty) {}
	}


	/**
	 * Helper method to parse an Object into an integer
	 * @param inObject object, eg from dialog
	 * @return int value given
	 */
	protected static int parseNumber(Object inObject)
	{
		int num = 0;
		if (inObject != null)
		{
			try
			{
				num = Integer.parseInt(inObject.toString());
			}
			catch (NumberFormatException nfe)
			{}
		}
		return num;
	}

	/**
	 * Display a standard error message
	 * @param inTitleKey key to lookup for window title
	 * @param inMessageKey key to lookup for error message
	 */
	public void showErrorMessage(String inTitleKey, String inMessageKey)
	{
		JOptionPane.showMessageDialog(_frame, I18nManager.getText(inMessageKey),
			I18nManager.getText(inTitleKey), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Display a standard error message
	 * @param inTitleKey key to lookup for window title
	 * @param inMessage error message
	 */
	public void showErrorMessageNoLookup(String inTitleKey, String inMessage)
	{
		JOptionPane.showMessageDialog(_frame, inMessage,
			I18nManager.getText(inTitleKey), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @param inViewport viewport object
	 */
	public void setViewport(Viewport inViewport)
	{
		_viewport = inViewport;
	}

	/**
	 * @return current viewport object
	 */
	public Viewport getViewport()
	{
		return _viewport;
	}

	/**
	 * Set the controller for the full screen mode
	 * @param inController controller object
	 */
	public void setSidebarController(SidebarController inController)
	{
		_sidebarController = inController;
	}

	/**
	 * Toggle sidebars on and off
	 */
	public void toggleSidebars()
	{
		if( _sidebarController != null )
		_sidebarController.toggle();
	}

	public File getOpenFile()
	{
		return _openFile;
	}

	public void setOpenFile(File inFile)
	{
		_openFile = inFile;
	}
	
	/**
	 * Inform the app that the data has been saved
	 */
	public void informDataSaved()
	{
		UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_SAVED);
	}
}
