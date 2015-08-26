package tim.prune;

import javax.swing.JFrame;

/**
 * Generic function class for launching from the app
 */
public abstract class GenericFunction
{
	protected App _app = null;
	/** Reference to app object */
	protected PruneApp _pruneApp = null;
	/** Reference to parent frame */
	protected JFrame _parentFrame = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GenericFunction(App inApp)
	{
		_app = inApp;
		if( inApp instanceof PruneApp )
			_pruneApp = (PruneApp)inApp;
		_parentFrame = inApp.getFrame();
	}

	/**
	 * Begin the function
	 */
	public abstract void begin();

	/**
	 * @return the key for the function name
	 */
	public abstract String getNameKey();
}
