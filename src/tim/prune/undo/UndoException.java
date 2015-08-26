package tim.prune.undo;

/**
 * Exception thrown when undo operation fails
 */
public class UndoException extends Exception
{
	private static final long serialVersionUID = 7989440313181523819L;

	/**
	 * Constructor
	 * @param inMessage description of operation which failed
	 */
	public UndoException(String inMessage)
	{
		super(inMessage);
	}
}
