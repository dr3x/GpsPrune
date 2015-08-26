package tim.prune.drawing.tool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import tim.prune.DrawApp;
import tim.prune.I18nManager;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.DrawingStyle;
import tim.prune.drawing.item.Group;
import tim.prune.drawing.tool.event.MapEventHandler;
import tim.prune.drawing.tool.property.PropertyTool;
import tim.prune.gui.IconManager;
import tim.prune.gui.map.MapCanvas;
import tim.prune.util.ListenerList;


public class Toolbox {
	
	public static interface ToolActivationListener {
		void onToolActivationChanged( Tool tool );
	}

	private final ListenerList<ToolActivationListener> listeners;
	private final List<Tool> tools = new ArrayList<Tool>();
	private final Map<Class<?>,PropertyTool> propertyTools = new HashMap<Class<?>,PropertyTool>();
	
	private DrawApp app;
	private Tool active;
	private MapEventHandler mapEventHandler;
	
	private DrawingStyle style;
	private JToolBar propertyToolbar;
	private JWindow propertyToolPopup;
	
	public Toolbox( DrawApp app ) {
		this.app = app;
		
		mapEventHandler = new MapEventDistributor();
		
		tools.add(new FreeLineTool(app));
		tools.add(new PolylineTool(app, false));
		tools.add(new PolylineTool(app, true));
		tools.add(new ArrowTool(app));
		tools.add(new RectangleTool(app));
		tools.add(new EllipseTool(app));
		tools.add(new TextTool(app));
		tools.add(new NumberTool(app));
		tools.add(new GroupTool(app));
		
		style = new DrawingStyle();
		
		listeners = new ListenerList<ToolActivationListener>();
		listeners.setNotifier(listeners.new Notifier() {
			@Override
			public void notify(ToolActivationListener listener) {
				listener.onToolActivationChanged(active);
			}
		});
		
		propertyToolPopup = new JWindow(app.getFrame()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public java.awt.Insets getInsets() {
				return new Insets(5, 5, 5, 5);
			}
		};
		propertyToolPopup.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				propertyToolPopup.setVisible(false);		
			}
		});
		for( Tool t : tools ) {
			for( Class<? extends PropertyTool> type : t.getPropertyToolTypes() ) {
				if( !propertyTools.containsKey(type) ) {
					try {
						Constructor<? extends PropertyTool> c = 
							type.getConstructor(DrawApp.class, JWindow.class);
						PropertyTool propertyTool = c.newInstance(app, propertyToolPopup);
						propertyTools.put(type, propertyTool);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public MapEventHandler getMapEventHandler() {
		return mapEventHandler;
	}
	
	public List<Tool> getTools() {
		return tools;
	}
	
	public DrawingStyle getStyle() {
		return style;
	}
	
	public void addTools( JPanel panel ) {
		JToolBar shapeToolBar = new JToolBar();

		JToggleButton toolButton = new JToggleButton();
		toolButton.setSelected(true);
		toolButton.setBorderPainted(false);
		DefaultToolListener defaultToolListener = new DefaultToolListener(this, toolButton);
		toolButton.addActionListener(defaultToolListener);
		getActivationListeners().add(defaultToolListener);
		toolButton.setIcon(IconManager.getImageForKey("tool.icon.null"));
		toolButton.setToolTipText(I18nManager.getText("tool.name.null"));
		shapeToolBar.add(toolButton);
		
		for( Tool tool : tools ) {
			toolButton = new JToggleButton();
			toolButton.setBorderPainted(false);
			ToolListener toolListener = new ToolListener(this, tool, toolButton);
			toolButton.addActionListener(toolListener);
			getActivationListeners().add(toolListener);
			toolButton.setIcon(IconManager.getImageForKey("tool.icon." + tool.getToolKey()));
			toolButton.setToolTipText(I18nManager.getText("tool.name." + tool.getToolKey()));
			shapeToolBar.add(toolButton);
		}
		panel.add(shapeToolBar);
		
		propertyToolbar = new JToolBar();
		propertyToolbar.setVisible(false);
		panel.add(propertyToolbar);
	}
	
	public void activate( Tool tool ) {
		deactivate();
		active = tool;
		
		if( active != null ) {
			// make sure we have an item to edit
			DrawingItem selected = app.getSelected();			
			reactivate(selected);
		}
	}

	/**
	 * Reactivate the existing active too creating a new item
	 * 
	 * @param selected
	 */
	public void reactivate() {
		reactivate(null);
	}

	/**
	 * Reactivate the existing active tool for a new item
	 * 
	 * @param selected
	 */
	public void reactivate(DrawingItem selected) {
		if( active == null ) {
			throw new IllegalStateException("No active tool");
		}
		
		active.deactivate();
		
		if( selected == null ) {
			selected = active.createItem();
			if( selected != null ) {
				app.setUndoEnabled(false);
				selected.applyStyle(app.getToolbox().getStyle());
				app.setUndoEnabled(true);
				active.setCreateMode(true);
			}
		} else {
			if( !active.isToolFor(selected) ) {
				throw new IllegalArgumentException("Active tool not valid");
			}
			selected.updateStyle(app.getToolbox().getStyle());	
		}
		app.setSelected(selected);

		// update active item
		active.setItem(selected);
		active.activate();

		// build toolbar
		List<Class<? extends PropertyTool>> types = active.getPropertyToolTypes();
		for( Class<? extends PropertyTool> type : types ) {
			PropertyTool propertyTool = propertyTools.get(type);
			if( propertyTool != null ) {
				propertyTool.addTools(propertyToolbar);
				propertyTool.setItem(active.getItem());
			}
		}
		propertyToolbar.setVisible(true);
		
		// notify listeners
		listeners.notifyListeners();
	}
	
	public void activateToolFor( DrawingItem item ) {
		Tool tool = findToolFor(item);
		if( tool != null ) {
			deactivate();
			app.setSelected(item);
			activate(tool);
		}
	}
	
	public void deactivate() {
		if( active != null ) {
			if( active.getItem() != null && active.getItem().getPoints().isEmpty() ) {
				app.getDrawing().removeItem(active.getItem());
			}
			app.setSelected(null);
			active.deactivate();
			listeners.notifyListeners();
		}
		active = null;
		
		propertyToolbar.removeAll();
		propertyToolbar.setVisible(false);
		propertyToolPopup.setVisible(false);
	}
	
	public Tool getActive() {
		return active;
	}
	
	public Tool findToolFor( DrawingItem item ) {
		for( Tool tool : tools ) {
			if( tool.isToolFor(item) ) {
				return tool;
			}
		}
		return null;
	}
	
	public ListenerList<ToolActivationListener> getActivationListeners() {
		return listeners;
	}
	
	private final class ToolListener implements ActionListener, ToolActivationListener {
		
		private Toolbox toolbox;
		private Tool tool;
		private JToggleButton button;
		private boolean changing;
		
		public ToolListener(Toolbox toolbox, Tool tool, JToggleButton button) {
			super();
			this.toolbox = toolbox;
			this.tool = tool;
			this.button = button;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if( !changing ) {
				changing = true;
				if( button.isSelected() ) {
					toolbox.activate(tool);
				} else {
					toolbox.deactivate();
				}
				changing = false;
			}	
		}

		@Override
		public void onToolActivationChanged(Tool tool) {
			if( !changing && tool == this.tool) {
				changing = true;
				button.setSelected(tool.isActive());
				changing = false;
			}
		}
	}
	
	private final class DefaultToolListener implements ActionListener, ToolActivationListener {
		
		private Toolbox toolbox;
		private JToggleButton button;
		private boolean changing;
		
		public DefaultToolListener(Toolbox toolbox, JToggleButton button) {
			super();
			this.toolbox = toolbox;
			this.button = button;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if( !changing ) {
				changing = true;
				if( button.isSelected() ) {
					toolbox.deactivate();
				}
				changing = false;
			}	
		}

		@Override
		public void onToolActivationChanged(Tool tool) {
			if( !changing ) {
				changing = true;
				button.setSelected(tool == null || !tool.isActive());
				changing = false;
			}
		}
	}
	
	private final class MapEventDistributor implements MapEventHandler {
		
		/**
		 * Used to ensure that we don't send a click event
		 * if we used the press event to activate a tool.
		 */
		private boolean skipClickEvent;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			app.setUndoEnabled(true);			
			if( !skipClickEvent ) {
				Tool active = getActive();
				if( active != null ) {
					active.getMapEventHandler().mouseClicked(e);
				}
			}
			skipClickEvent = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if( getActive() != null ) {
				getActive().getMapEventHandler().mousePressed(e);
				if( !e.isConsumed() && getActive() != null ) {
					if( getActive().isCreateMode() ) {
						if( activateTool(e) ) {
							skipClickEvent = true;
						} else {
							reactivate();
							skipClickEvent = true;
						}
						if( getActive() != null ) {
							getActive().getMapEventHandler().mousePressed(e);
						}
					}
				}
			} else if( activateTool(e) ){
				skipClickEvent = true;
				getActive().getMapEventHandler().mousePressed(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Tool active = getActive();
			if( active != null ) {
				active.getMapEventHandler().mouseReleased(e);
			}
			app.setUndoEnabled(true);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			skipClickEvent = false;
			
			Tool active = getActive();
			if( active != null ) {
				active.getMapEventHandler().mouseDragged(e);
			}
			
			if( e.isConsumed() ) {
				app.setUndoEnabled(false);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			skipClickEvent = false;
			
			MapCanvas canvas = app.getCanvas();
			Drawing drawing = app.getDrawing();
			Tool active = getActive();
			
			for( DrawingItem item : drawing.getItems() )
				item.setHighlight(null);
			
			DrawingItem hit = drawing.hit(canvas, (Graphics2D) 
					canvas.getGraphics(), e.getX(), e.getY());
			if( hit != null ) {
				hit.setHighlight(new Color(0, 255, 255, 255));
			}
			
			if( active != null ) {
				active.getMapEventHandler().mouseMoved(e);
				
			} else if( hit != null ){
				Tool tool = findToolFor(hit);
				if( tool != null ) {
					tool.getMapEventHandler().mouseMoved(e);
				}
			} else {
				canvas.setCursor(Cursor.getDefaultCursor());
			}
			
			if( e.isConsumed() ) {
				app.setUndoEnabled(false);
			}
			
			canvas.repaint();
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			Tool active = getActive();
			if( active != null ) {
				active.getMapEventHandler().keyPressed(e);
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			Tool active = getActive();
			if( active != null ) {
				active.getMapEventHandler().keyReleased(e);
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			Tool active = getActive();
			if( active != null ) {
				active.getMapEventHandler().keyTyped(e);
			}
		}

		private boolean activateTool( MouseEvent e ) {
			Drawing drawing = app.getDrawing();
			DrawingItem selected = app.getSelected();
			MapCanvas canvas = app.getCanvas();
			Graphics2D graphics = (Graphics2D)canvas.getGraphics();

			if( drawing != null && selected == null ) {
				DrawingItem hit = drawing.hit(canvas, graphics, e.getX(), e.getY());
				
				if( hit == null && e.isShiftDown() ) {
					hit = drawing.addItem(Group.class);
				}
				
				if( hit != null ) {
					Tool tool = findToolFor(hit);
					if( tool != null ) {
						app.setSelected(hit);
						activate(tool);
						return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {		
		}
	}
}
