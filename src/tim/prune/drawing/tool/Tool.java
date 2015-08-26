package tim.prune.drawing.tool;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.tool.event.DelegatingMapEventHandler;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.DragHandles;
import tim.prune.drawing.tool.event.MapEventHandler;
import tim.prune.drawing.tool.event.MapEventHandlerDelegate;
import tim.prune.drawing.tool.property.PropertyTool;
import tim.prune.gui.map.MapCanvas;


public abstract class Tool {

	private final String toolKey;
	private final DrawApp app;
	private final List<Class<? extends PropertyTool>> propertyToolTypes;
	private final DelegatingMapEventHandler eventHandler;
	
	private int handleTypes;
	private DrawingItem item;
	private DragHandles handles;
	private boolean active;
	private boolean createMode;
	
	public Tool(String toolKey, DrawApp app) {
		this.app = app;
		this.toolKey = toolKey;
		this.eventHandler = new DelegatingMapEventHandler();
		this.handles = DragHandles.NULL;
		this.propertyToolTypes = new LinkedList<Class<? extends PropertyTool>>();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String getToolKey() {
		return toolKey;
	}
	
	public DrawingItem getItem() {
		return item;
	}
	
	public void setItem(DrawingItem item) {
		this.item = item;
	}
	
	public void setCreateMode(boolean newItem) {
		this.createMode = newItem;
	}
	
	public boolean isCreateMode() {
		return createMode;
	}
	
	public DragHandles getHandles() {
		return handles;
	}
	
	public void setHandles(DragHandles handles) {
		this.handles = handles == null ? DragHandles.NULL : handles;
	}
	
	public DrawApp getApp() {
		return app;
	}
	
	public MapEventHandler getMapEventHandler() {
		return eventHandler;
	}
	
	public List<Class<? extends PropertyTool>> getPropertyToolTypes() {
		return propertyToolTypes;
	}
	
	public void removePoint( MapCanvas canvas, Graphics2D graphics, int x, int y ) {
		DrawingItem selected = app.getSelected();				
		if( selected != null ) {
			DragHandle handle = handles.hit(canvas, graphics, x, y);
			if( handle != null && handle.getType() == DragHandle.TYPE_POINT ) {
				if( selected.getPoints().size() <= 1 ) {
					app.deleteSelected();
				} else {
					Integer data = (Integer) handle.getData();
					selected.removePoint(data);
					handles.update();
				}
			}
		}
	}
	
	protected void activate() {
		if( item != null ) {
			handles = new DragHandles(item, handleTypes);
			handles.update();
			app.getOverlays().addOverlay(handles);
		}
		active = true;
	}
	
	protected void deactivate() {
		item = null;
		if( handles != null ) {
			app.getOverlays().removeOverlay(handles);
		}
		handles = DragHandles.NULL;		
		active = false;
		createMode = false;
	}
	
	protected void registerMapEventHandlerDelegate( MapEventHandlerDelegate d ) {
		d.register(eventHandler);
	}
	
	protected void registerPropertyToolType( Class<? extends PropertyTool> type ) {
		propertyToolTypes.add(type);
	}
	
	protected void registerHandleType(int handleType) {
		handleTypes |= handleType;
	}
	
	protected abstract DrawingItem createItem();
	
	public abstract boolean isToolFor( DrawingItem item );
}
