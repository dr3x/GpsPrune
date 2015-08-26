package tim.prune;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import tim.prune.config.Config;
import tim.prune.data.FileInfo;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.Drawing.ChangeListener;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.ItemFactory;
import tim.prune.drawing.tool.Toolbox;
import tim.prune.gui.map.Projection;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.DrawingWriter;
import tim.prune.util.ListenerList;

public class DrawApp extends App implements ChangeListener {	
	
	public static interface SelectionListener {
		void onSelected( DrawingItem item );
	}
	
	private final ListenerList<SelectionListener> selectionListeners;
	
	private ItemFactory factory;
	private Toolbox toolbox;
	private Drawing drawing;
	private DrawingItem selected;
	private DrawingItem copied;
	
	private boolean append;
	private boolean wasDirty;
	private File originalFile;
	
	public DrawApp(JFrame inFrame) {
		super(inFrame, new File(Config.getConfigString(Config.KEY_BASE_FILE_LOCATION) 
				+ "/Drawings/"));
		
		factory = new ItemFactory(this);
		toolbox = new Toolbox(this);
		drawing = new Drawing(this);
		selectionListeners = new ListenerList<DrawApp.SelectionListener>();
		selectionListeners.setNotifier(selectionListeners.new Notifier() {
			@Override
			public void notify(SelectionListener listener) {
				listener.onSelected(selected);
			}
		});
		
		drawing.getChangeListeners().add(this);
		getFrame().setTitle("Untitled");
		getOverlays().addOverlay(drawing);
		FunctionLibrary.initialise(this);		
	}
	
	public ItemFactory getFactory() {
		return factory;
	}
	
	public Toolbox getToolbox() {
		return toolbox;
	}
	
	public Drawing getDrawing() {
		return drawing;
	}
	
	public DrawingItem getSelected() {
		return selected;
	}
	
	public void setSelected(DrawingItem selected) {
		DrawingItem old = this.selected;
		this.selected = selected;
		if( old != selected ) {
			selectionListeners.notifyListeners();
		}
	}
	
	@Override
	public void loadDataFiles(ArrayList<File> inDataFiles) {
		super.loadDataFiles(inDataFiles);
		if( inDataFiles == null || inDataFiles.isEmpty() ) {
			newFile();
		}
	}

	@Override
	public boolean hasDataUnsaved() {
		return drawing.isDirty();
	}
	
	@Override
	public void saveFile(boolean saveAs) {
		File selectedFile = getOpenFile();		
		if( saveAs || selectedFile == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(getBaseDir(true));
			chooser.setFileFilter(new GenericFileFilter("filetype.kml", new String[] {"kml"}));

			if( selectedFile != null ) {
				chooser.setSelectedFile(selectedFile);
			}
			
			if( chooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION ) {
				selectedFile = chooser.getSelectedFile();
			}
		}
		
		if( selectedFile != null ) {
			if( !selectedFile.getName().endsWith(".kml") ) {
				selectedFile = new File(selectedFile.getAbsolutePath() + ".kml");
			}
			
			DrawingWriter writer = new DrawingWriter(drawing);
			try {
				writer.write(selectedFile);				
				setOpenFile(selectedFile);
				updateTitle(Collections.singletonList(selectedFile));
				drawing.setDirty(false);
				informDataSaved();
			} catch (Exception e) {
				showErrorMessageNoLookup("error.save.dialogtitle", "Failed to save file.  " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onFileLoaded(FileInfo info) {
		getUndoStack().clear();
		getMenuManager().informFileLoaded();
		toolbox.deactivate();
		
		getCanvas().zoomToFit();
		loadNextFile();
		super.onFileLoaded(info);
	}
	
	@Override
	protected void loadComplete() {
		if( append ) {
			drawing.setDirty(wasDirty);
			setOpenFile(originalFile);
			if( originalFile == null ) {
				updateTitle();
			} else {
				updateTitle(Collections.singletonList(originalFile));
			}
		} else {			
			drawing.setDirty(false);		
		}
		
		originalFile = null;
		wasDirty = false;
		append = false;
		onChange(drawing);
		super.loadComplete();
	}
	
	@Override
	public void openFile() {
		if( confirmDataLoss("opening another file") ) {
			drawing.clearItems();
			drawing.setDirty(false);
			super.openFile(true);
		}
	}
	
	public void appendFile() {
		append = true;
		wasDirty = hasDataUnsaved();
		originalFile = getOpenFile();
		super.openFile(true);
	}
	
	public void newFile() {
		if( confirmDataLoss("starting a new drawing") ) {
			setOpenFile(null);
			drawing.clearItems();
			drawing.setDirty(false);
			selected = null;
			copied = null;
			toolbox.deactivate();
			updateTitle();
			getCanvas().repaint();
		}
	}

	public void deleteSelected() {
		if( selected != null ) {			
			drawing.removeItem(selected);
			toolbox.deactivate();
			selected = null;
		}
	}
	
	public boolean copySelected() {
		if( selected != null ) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				selected.writeExternal(oos);
				oos.flush();
				
				copied = factory.createItem(selected.getClass());
				copied.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
				return true;
			} catch ( Exception thisWontHappen ) {
				thisWontHappen.printStackTrace();
				throw new RuntimeException(thisWontHappen);
			}
		}
		return false;
	}
	
	public void pasteSelected() {
		if( copied != null ) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				copied.writeExternal(oos);
				oos.flush();

				DrawingItem item = drawing.addItem(copied.getClass());
				long id = item.getId();
				item.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
				item.setId(id);
				
				// offset by a bit
				Projection projection = getCanvas().getProjection();
				if( projection != null ) {
					Point2D.Double reuse = new Point2D.Double();
					List<Double> points = item.getPoints();
					for( Point2D.Double p : points ) {
						projection.toScreen(p.y, p.x, reuse);
						reuse.x -= 20;
						reuse.y -= 20;
						projection.fromScreen((int)reuse.x, (int)reuse.y, reuse);
						p.x = reuse.x;
						p.y = reuse.y;
					}
				}
				
				toolbox.activateToolFor(item);
				
				getCanvas().repaint();
			} catch ( Exception thisWontHappen ) {
				thisWontHappen.printStackTrace();
				throw new RuntimeException(thisWontHappen);
			}
		}
	}
	
	public ListenerList<SelectionListener> getSelectionListeners() {
		return selectionListeners;
	}
	
	@Override
	public void onChange(Drawing drawing) {
		String title = getFrame().getTitle();
		if( drawing.isDirty() && !title.startsWith("* ") ) {
			getFrame().setTitle("* " + title);
		} else if( !drawing.isDirty() && title.startsWith("* ") ) {
			String newTitle = title;
			if( newTitle.length() > 2 ) {
				newTitle = title.substring(2);
			} else {
				newTitle = "";
			}
			getFrame().setTitle(newTitle);
		}
		getMenuManager().dataUpdated(DataSubscriber.ALL);
	}
}
