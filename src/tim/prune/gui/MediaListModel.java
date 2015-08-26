package tim.prune.gui;

import javax.swing.AbstractListModel;

import tim.prune.data.MediaFile;
import tim.prune.data.MediaList;

/**
 * Class to act as list model for the photo list and audio list
 */
public class MediaListModel extends AbstractListModel
{
	private static final long serialVersionUID = 4814298988714854445L;

	/** media list */
	MediaList _media = null;

	/**
	 * Constructor giving MediaList object
	 * @param inList MediaList
	 */
	public MediaListModel(MediaList inList) {
		_media = inList;
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return _media.getNumMedia();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int inIndex)
	{
		MediaFile m = _media.getMedia(inIndex);
		// * means modified since loading
		return (m.getCurrentStatus() == m.getOriginalStatus()?"":"* ") + m.getFile().getName();
	}

	/**
	 * Fire event to notify that contents have changed
	 */
	public void fireChanged() {
		this.fireContentsChanged(this, 0, getSize()-1);
	}
}
