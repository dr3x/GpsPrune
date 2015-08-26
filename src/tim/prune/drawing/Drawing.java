package tim.prune.drawing;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import tim.prune.DrawApp;
import tim.prune.draw.Hitable;
import tim.prune.drawing.undo.UndoDrawingEdit;
import tim.prune.gui.map.MapCanvas;
import tim.prune.overlay.Overlay;
import tim.prune.util.ListenerList;

public class Drawing extends Overlay implements Hitable {

	public static interface ChangeListener {
		void onChange( Drawing drawing );
	}
	
	private final List<DrawingItem> items = new ArrayList<DrawingItem>();
	private final ListenerList<ChangeListener> changeListeners;		
	private final DrawApp app;
	
	private boolean dirty;
	
	public Drawing( DrawApp app ) {
		this.app = app;
		
		changeListeners = new ListenerList<ChangeListener>();
		changeListeners.setNotifier(changeListeners.new Notifier() {
			@Override
			public void notify(ChangeListener listener) {
				listener.onChange(Drawing.this);
			}
		});		
	}
	
	public List<DrawingItem> getItems() {
		return items;
	}
	
	public DrawApp getApp() {
		return app;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		changeListeners.notifyListeners();
	}
	
	public <T extends DrawingItem> T addItem( Class<T> itemType ) {
		T item = app.getFactory().createItem(itemType);
		item.setId(System.currentTimeMillis());
		app.getUndoStack().add(new UndoDrawingEdit(UndoDrawingEdit.OP_CREATE, item));		
		item.setDrawing(this);
		items.add(item);
		setDirty(true);
		return item;
	}
	
	public void removeItem( DrawingItem item ) {		
		if( items.remove(item) ) {
			app.getUndoStack().add(new UndoDrawingEdit(UndoDrawingEdit.OP_DELETE, item));
			setDirty(true);	
		}
	}
	
	public void replaceItem( DrawingItem item ) {
		int indexOf = items.indexOf(item);
		if( indexOf > -1 ) {
			items.set(indexOf, item);
		}
		item.setDrawing(this);
		setDirty(true);
	}
	
	public void sendToBack( DrawingItem item ) {
		if( items.remove(item) )
			items.add(0, item);
	}
	
	public void bringToFront( DrawingItem item ) {
		if( items.remove(item) )
			items.add(item);
	}
	
	public void clearItems() {
		items.clear();
		setDirty(true);		
	}
	
	public ListenerList<ChangeListener> getChangeListeners() {
		return changeListeners;
	}
	
	@Override
	public DrawingItem hit( MapCanvas canvas, Graphics2D graphics, int x, int y ) {
		for( int i = items.size()-1; i > -1; i-- ) {
			DrawingItem item = items.get(i);
			item = item.hit(canvas, graphics, x, y);
			if( item != null )
				return item;
		}
		return null;
	}
	
	@Override
	protected void onDraw(MapCanvas canvas, Graphics2D graphics) {
		for( int i = 0; i < items.size(); i++ ) {
			DrawingItem drawingItem = items.get(i);
			drawingItem.draw(canvas, graphics);
		}
	}

	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double rect = null;
		for( DrawingItem item : items )
			for( Point2D.Double p : item.getPoints() ) {
				if( rect == null )
					rect = new Rectangle2D.Double(p.x, p.y, 0, 0);
				rect.add(p);
			}
		return rect;
	}
}
