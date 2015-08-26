package tim.prune.gui.map;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D.Double;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tim.prune.DrawApp;
import tim.prune.data.DoubleRange;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Group;
import tim.prune.drawing.tool.Tool;
import tim.prune.drawing.tool.Toolbox;
import tim.prune.drawing.tool.Toolbox.ToolActivationListener;

public class DrawingMapCanvas extends MapCanvas implements ToolActivationListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DrawApp app;
	private Toolbox toolbox;
	
	private JMenuItem deleteItem;
	private JMenuItem deletePoint;
	private JMenuItem toBack;
	private JMenuItem toFront;
	private JMenuItem selectAllOfType;

	public DrawingMapCanvas(DrawApp app) {
		super(app);
		this.app = app;
		this.toolbox = app.getToolbox();
		this.toolbox.getActivationListeners().add(this);
	}

	@Override
	protected JPopupMenu buildContextMenu() {
		JPopupMenu menu = super.buildContextMenu();
		menu.addSeparator();
		toBack = new JMenuItem("Send to back");
		toBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				DrawingItem item = app.getDrawing().hit(
						DrawingMapCanvas.this, (Graphics2D)getGraphics(), 
							_popupMenuX, _popupMenuY);
				if( item != null ) {
					app.getDrawing().sendToBack(item);
				}
				repaint();
			}});
		menu.add(toBack);
		toFront = new JMenuItem("Bring to front");
		toFront.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				DrawingItem item = app.getDrawing().hit(
						DrawingMapCanvas.this, (Graphics2D)getGraphics(), 
							_popupMenuX, _popupMenuY);
				if( item != null ) {
					app.getDrawing().bringToFront(item);
				}
				repaint();
			}});
		menu.add(toFront);	
		menu.addSeparator();
		deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				DrawingItem item = app.getDrawing().hit(
						DrawingMapCanvas.this, (Graphics2D)getGraphics(), 
							_popupMenuX, _popupMenuY);
				if( item != null ) {
					app.getDrawing().removeItem(item);
					app.getToolbox().deactivate();
				}
				repaint();
			}});
		menu.add(deleteItem);
		deletePoint = new JMenuItem("Delete Point");
		deletePoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				DrawingItem selected = app.getSelected();				
				Tool tool = toolbox.getActive();
				if( selected != null && tool != null ) {
					tool.removePoint(DrawingMapCanvas.this, 
							(Graphics2D)getGraphics(), 
							_popupMenuX, _popupMenuY);
					repaint();
				}
			}});
		menu.add(deletePoint);
		
		menu.addSeparator();
		selectAllOfType = new JMenuItem("Select All of Type");
		selectAllOfType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Drawing drawing = app.getDrawing();
				DrawingItem selected = app.getSelected();
				Class<?> class1 = selected.getClass().getSuperclass();
				Group group = drawing.addItem(Group.class);
				for( DrawingItem item : drawing.getItems() ) {					
					Class<?> class2 = item.getClass().getSuperclass();
					if( class1 == class2 ) {
						group.getItems().add(item);
					}
				}
				if( !group.getItems().isEmpty() ) {
					toolbox.activateToolFor(group);
					repaint();
				} else {					
					drawing.removeItem(group);
				}
			}});
		menu.add(selectAllOfType);

		menu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				DrawingItem item = app.getDrawing().hit(
						DrawingMapCanvas.this, (Graphics2D)getGraphics(), 
							_popupMenuX, _popupMenuY);
				
				DrawingItem selected = app.getSelected();
				toBack.setEnabled(item != null);
				toFront.setEnabled(item != null);
				deleteItem.setEnabled(item != null);
				deletePoint.setEnabled(toolbox.getActive() != null);
				selectAllOfType.setEnabled(selected != null && !(selected instanceof Group));
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) { }
		});
		
		return menu;
	}

	@Override
	public void zoomToFit() {
		Drawing drawing = app.getDrawing();
		Double bounds = drawing.getBounds();
		if( bounds == null )
			bounds = new Double();
		if( bounds.isEmpty() ) {
			bounds.x = -90;
			bounds.y = -45;
			bounds.width = 180;
			bounds.height = 90;
		}
		DoubleRange xrange = new DoubleRange(MapUtils.getXFromLongitude(bounds.getMinX()),
				MapUtils.getXFromLongitude(bounds.getMaxX()));
		DoubleRange yrange = new DoubleRange(MapUtils.getYFromLatitude(bounds.getMinY()),
				MapUtils.getYFromLatitude(bounds.getMaxY()));
		getMapPosition().zoomToXY(xrange.getMinimum(), xrange.getMaximum(), 
				yrange.getMinimum(), yrange.getMaximum(),
				getWidth(), getHeight());

		_recalculate = true;
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocus();
		toolbox.getMapEventHandler().mouseClicked(e);
		
		if( !e.isConsumed() ) {
			super.mouseClicked(e);
		} else {
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {		
		requestFocus();
		toolbox.getMapEventHandler().mousePressed(e);
		if( !e.isConsumed() ) {
			super.mousePressed(e);
		} else {
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {		
		toolbox.getMapEventHandler().mouseReleased(e);
		if( !e.isConsumed() ) {
			super.mouseReleased(e);
		} else {
			repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		toolbox.getMapEventHandler().mouseDragged(e);
		if( !e.isConsumed() ) {
			super.mouseDragged(e);
		} else {
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		toolbox.getMapEventHandler().mouseMoved(e);
		if( !e.isConsumed() ) {			
			super.mouseMoved(e);
		} else {
			repaint();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		toolbox.getMapEventHandler().keyPressed(e);
		if( !e.isConsumed() ) {
			super.keyPressed(e);
		} else {
			repaint();
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		toolbox.getMapEventHandler().keyReleased(e); 
		if( !e.isConsumed() ) {
			super.keyReleased(e);
		} else {
			repaint();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		toolbox.getMapEventHandler().keyTyped(e);
		if( !e.isConsumed() ) {
			super.keyTyped(e);
		} else {
			repaint();
		}
	}

	@Override
	public void onToolActivationChanged(Tool tool) {
		repaint();
	}
}
