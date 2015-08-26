package tim.prune.drawing.tool;

import java.awt.AWTEvent;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.item.Number;
import tim.prune.drawing.item.Text;
import tim.prune.drawing.tool.event.DelegatingMapEventHandler;
import tim.prune.drawing.tool.event.DragHandle;
import tim.prune.drawing.tool.event.MapEventHandlerDelegate;
import tim.prune.drawing.tool.event.Mover;
import tim.prune.drawing.tool.event.PointAdder;
import tim.prune.drawing.tool.event.Rotator;
import tim.prune.drawing.tool.event.ToolDeactivator;
import tim.prune.drawing.tool.property.BackgroundPropertyTool;
import tim.prune.drawing.tool.property.FontSizePropertyTool;
import tim.prune.drawing.tool.property.FontStylePropertyTool;
import tim.prune.drawing.tool.property.ForegroundPropertyTool;
import tim.prune.drawing.tool.property.TextPropertyTool;
import tim.prune.gui.map.MapCanvas;

public class TextTool extends Tool {

	private int counter;
	private TextToolWindow window;
	
	public TextTool( DrawApp app ) {
		super("text", app );
		
//		registerMapEventHandlerDelegate(new TextEditor(this));
		registerMapEventHandlerDelegate(new PointAdder(this, 1));
		registerMapEventHandlerDelegate(new Mover(this));
		registerMapEventHandlerDelegate(new Rotator(this));
		registerMapEventHandlerDelegate(new ToolDeactivator(this, -1));
		
		registerHandleType(DragHandle.TYPE_ROTATE);
		registerHandleType(DragHandle.TYPE_MOVE);
		
		registerPropertyToolType(ForegroundPropertyTool.class);
		registerPropertyToolType(BackgroundPropertyTool.class);
		registerPropertyToolType(FontSizePropertyTool.class);
		registerPropertyToolType(FontStylePropertyTool.class);
		registerPropertyToolType(TextPropertyTool.class);
	}

	@Override
	protected DrawingItem createItem() {
		Text item = getApp().getDrawing().addItem(Text.class);
		item.setText("Text " + (++counter));
		return item;
	}

	@Override
	public boolean isToolFor(DrawingItem itemType) {
		return itemType instanceof Text &&
			(!(itemType instanceof Number));
	}
	
	public class TextEditor extends MapEventHandlerDelegate {
		public TextEditor(Tool tool) {
			super(tool);
		}
		
		@Override
		public void register(DelegatingMapEventHandler handler) {
			handler.register(MouseEvent.MOUSE_CLICKED, this);
		}

		@Override
		public void handleEvent(AWTEvent e) {
			MouseEvent me = (MouseEvent) e;
			MapCanvas canvas = getApp().getCanvas();
			Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			
			DrawingItem item = getItem().hit(canvas, graphics, me.getX(), me.getY());
			if( item == getItem() ) {
				// show text editor
				if( window != null ) {
					window.dispose();
				}
				
				JFrame frame = getApp().getFrame();				
				window = new TextToolWindow(frame, (Text)item);
				window.setVisible(true);
			}
		}
	}
}
