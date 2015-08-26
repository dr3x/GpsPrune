package tim.prune.drawing.tool.event;

import java.awt.AWTEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DelegatingMapEventHandler implements MapEventHandler {

	private final Map<Integer,List<MapEventHandlerDelegate>> delegates;

	public DelegatingMapEventHandler() {
		this.delegates = new HashMap<Integer, List<MapEventHandlerDelegate>>();
	}
	
	
	public void register( int eventType, MapEventHandlerDelegate d ) {
		List<MapEventHandlerDelegate> list = delegates.get(eventType);
		if( list == null ) {
			list = new LinkedList<MapEventHandlerDelegate>();
			delegates.put(eventType, list);
		}
		list.add(d);
	}
	
	
	@Override
	public void mouseDragged(MouseEvent e) {
		fireEvent(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		fireEvent(e);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		fireEvent(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		fireEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		fireEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		fireEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		fireEvent(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		fireEvent(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		fireEvent(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		fireEvent(e);
	}

	private void fireEvent( AWTEvent e ) {
		List<MapEventHandlerDelegate> delegates = this.delegates.get(e.getID());
		if( delegates != null ) {
			for( int i = 0; i < delegates.size(); i++ ) {
				MapEventHandlerDelegate d = delegates.get(i);
				d.handleEvent(e);
				if( e instanceof InputEvent && ((InputEvent) e).isConsumed() ) {
					return;
				}
			}
		}
	}
}
