package tim.prune.data;

import tim.prune.load.TrackNameList;
import tim.prune.load.xml.GpxMetadata;

public class TrackFileInfo extends FileInfo {

	private String[][] dataArray;
	private Field[] fieldArray;
	private TrackNameList trackNameList;
	private String[] linkArray;
	private GpxMetadata _gpxMetadata;
	
	public TrackFileInfo() {
	}

	public TrackFileInfo(String[][] dataArray, Field[] fieldArray,
			TrackNameList trackNameList, String[] linkArray) {
		this(dataArray, fieldArray, trackNameList, linkArray, null);
	}

	public TrackFileInfo(String[][] dataArray, Field[] fieldArray,
			TrackNameList trackNameList, String[] linkArray, GpxMetadata gpxMetadata) {
		super();
		this.dataArray = dataArray;
		this.fieldArray = fieldArray;
		this.trackNameList = trackNameList;
		this.linkArray = linkArray;
		this._gpxMetadata = gpxMetadata;
	}

	/**
	 * Method for returning data loaded from file
	 * @return 2d String array containing data
	 */
	public String[][] getDataArray() {
		return dataArray;
	}

	/**
	 * @return field array describing fields of data
	 */
	public Field[] getFieldArray() {
		return fieldArray;
	}

	/**
	 * Can be overriden (eg by gpx handler) to provide a track name list
	 * @return track name list object if any, or null
	 */
	public TrackNameList getTrackNameList() {
		return trackNameList;
	}

	/**
	 * Can be overriden (eg by gpx handler) to provide an array of links to media
	 * @return array of Strings if any, or null
	 */
	public String[] getLinkArray() {
		return linkArray;
	}
	
	public void setDataArray(String[][] dataArray) {
		this.dataArray = dataArray;
	}
	
	public void setFieldArray(Field[] fieldArray) {
		this.fieldArray = fieldArray;
	}
	
	public void setLinkArray(String[] linkArray) {
		this.linkArray = linkArray;
	}
	
	public void setTrackNameList(TrackNameList trackNameList) {
		this.trackNameList = trackNameList;
	}

	public GpxMetadata getGpxMetaData() {
		return _gpxMetadata;
	}
}
