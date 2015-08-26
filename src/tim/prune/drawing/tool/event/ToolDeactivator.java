package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import tim.prune.drawing.tool.Tool;

public class ToolDeactivator extends MapEventHandlerDelegate {

	private final int maxPoints;
	
	public ToolDeactivator( Tool tool, int maxPoints ) {
		super( tool );
		this.maxPoints = maxPoints;
	}
	
	@Override
	public void register(DelegatingMapEventHandler handler) {
		handler.register(KeyEvent.KEY_RELEASED, this);
		handler.register(MouseEvent.MOUSE_CLICKED, this);
	}
	
	@Override
	public void handleEvent(AWTEvent e) {
		if( e instanceof MouseEvent ) {
			
			MouseEvent mouseEvent = (MouseEvent) e;
			int clickCount = mouseEvent.getClickCount();
			
			if( clickCount == 2 ) {
				getTool().getApp().getToolbox().deactivate();
				mouseEvent.consume();
				
			} else if( clickCount == 1 && maxPoints > -1 && getItem().getPoints().size() >= maxPoints ) {
				getTool().getApp().getToolbox().deactivate();
				mouseEvent.consume();
				
			} else if( !getTool().isCreateMode() ) {
				getTool().getApp().getToolbox().deactivate();
				mouseEvent.consume();				
			}
			
		} else if( e instanceof KeyEvent && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_ESCAPE ) {
			getTool().getApp().getToolbox().deactivate();
		}
	}
}
