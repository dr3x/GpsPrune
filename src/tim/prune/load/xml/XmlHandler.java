package tim.prune.load.xml;

import org.xml.sax.helpers.DefaultHandler;

import tim.prune.data.FileInfo;

/**
 * Abstract superclass of xml handlers
 */
public abstract class XmlHandler extends DefaultHandler
{
	public abstract FileInfo getFileInfo();
}
