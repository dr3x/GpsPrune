package tim.prune.util;

import java.util.ArrayList;

public class ListenerList<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract class Notifier {
		public abstract void notify( T listener );
	}
	
	private Notifier notifier;
	
	public void setNotifier(Notifier notifier) {
		this.notifier = notifier;
	}
	
	public void notifyListeners() {
		if( notifier != null ) {
			for( int i = 0; i < size(); i++ )
				try {
					notifier.notify( get(i) );
				} catch ( Exception e ) {
					System.err.println("Failed to notify " + get(i));
					e.printStackTrace();
				}
		}
	}
}
