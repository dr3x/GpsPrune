package tim.prune.load.xml;

/**
 * Class to contain GPX metadata we care about.
 * This class is immutable.
 */
public class GpxMetadata {
	private String _version;
	private String _creator;
	private String _name;
	private String _desc;

	public GpxMetadata() {
		this(null, null, null, null);
	}

	public GpxMetadata(String version, String creator, String name, 
			String desc) {
		_version = version==null?"1.0":version;
		_creator = creator==null?"":creator;
		_name = name==null?"":name;
		_desc = desc==null?"":desc;
	}

	public String getVersion() {
		return _version;
	}

	public String getCreator() {
		return _creator;
	}

	public String getName() {
		return _name;
	}

	public String getDesc() {
		return _desc;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = true;
		GpxMetadata d = (GpxMetadata)o;
		ret = ret && _version.equals(d._version);
		ret = ret && _creator.equals(d._creator);
		ret = ret && _name.equals(d._name);
		ret = ret && _desc.equals(d._desc);
		return ret;
	}
}
