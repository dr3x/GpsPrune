package tim.prune;

import tim.prune.function.AboutScreen;
import tim.prune.function.AddAltitudeOffset;
import tim.prune.function.AddTimeOffset;
import tim.prune.function.ConvertNamesToTimes;
import tim.prune.function.DeleteFieldValues;
import tim.prune.function.DiskCacheConfig;
import tim.prune.function.DuplicatePoint;
import tim.prune.function.FindWaypoint;
import tim.prune.function.FullRangeDetails;
import tim.prune.function.HelpScreen;
import tim.prune.function.PasteCoordinates;
import tim.prune.function.RearrangeWaypointsFunction;
import tim.prune.function.SaveConfig;
import tim.prune.function.SetColours;
import tim.prune.function.SetKmzImageSize;
import tim.prune.function.SetLanguage;
import tim.prune.function.SetLineWidth;
import tim.prune.function.SetMapBgFunction;
import tim.prune.function.SetPathsFunction;
import tim.prune.function.ShowKeysScreen;
import tim.prune.function.charts.Charter;
import tim.prune.function.compress.CompressTrackFunction;
import tim.prune.function.distance.DistanceFunction;
import tim.prune.function.edit.PointNameEditor;
import tim.prune.function.srtm.LookupSrtmFunction;
import tim.prune.load.GpsLoader;
import tim.prune.save.GpsSaver;
import tim.prune.save.GpxExporter;
import tim.prune.save.KmlExporter;

/**
 * Class to provide access to functions
 */
public abstract class FunctionLibrary
{
	public static GenericFunction FUNCTION_GPXEXPORT = null;
	public static GenericFunction FUNCTION_KMLEXPORT = null;
	public static GenericFunction FUNCTION_GPSLOAD  = null;
	public static GenericFunction FUNCTION_GPSSAVE  = null;
	public static GenericFunction FUNCTION_SAVECONFIG  = null;
	public static GenericFunction FUNCTION_EDIT_WAYPOINT_NAME = null;
	public static RearrangeWaypointsFunction FUNCTION_REARRANGE_WAYPOINTS = null;
	public static GenericFunction FUNCTION_COMPRESS = null;
	public static GenericFunction FUNCTION_LOOKUP_SRTM = null;
	public static GenericFunction FUNCTION_ADD_TIME_OFFSET  = null;
	public static GenericFunction FUNCTION_ADD_ALTITUDE_OFFSET  = null;
	public static GenericFunction FUNCTION_CONVERT_NAMES_TO_TIMES  = null;
	public static GenericFunction FUNCTION_DELETE_FIELD_VALUES  = null;
	public static GenericFunction FUNCTION_PASTE_COORDINATES = null;
	public static GenericFunction FUNCTION_FIND_WAYPOINT = null;
	public static GenericFunction FUNCTION_DUPLICATE_POINT = null;
	public static GenericFunction FUNCTION_CHARTS = null;
	public static GenericFunction FUNCTION_DISTANCES  = null;
	public static GenericFunction FUNCTION_FULL_RANGE_DETAILS = null;
	public static GenericFunction FUNCTION_SET_MAP_BG = null;
	public static GenericFunction FUNCTION_SET_DISK_CACHE = null;
	public static GenericFunction FUNCTION_SET_PATHS  = null;
	public static GenericFunction FUNCTION_SET_KMZ_IMAGE_SIZE = null;
	public static GenericFunction FUNCTION_SET_COLOURS = null;
	public static GenericFunction FUNCTION_SET_LINE_WIDTH = null;
	public static GenericFunction FUNCTION_SET_LANGUAGE = null;
	public static GenericFunction FUNCTION_HELP   = null;
	public static GenericFunction FUNCTION_SHOW_KEYS = null;
	public static GenericFunction FUNCTION_ABOUT  = null;


	/**
	 * Initialise library of functions
	 * @param inApp App object to give to functions
	 */
	public static void initialise(App inApp)
	{
		if( inApp instanceof PruneApp ) {
			FUNCTION_GPXEXPORT = new GpxExporter(inApp);
			FUNCTION_KMLEXPORT = new KmlExporter(inApp);
		}
		FUNCTION_GPSLOAD   = new GpsLoader(inApp);
		FUNCTION_GPSSAVE   = new GpsSaver(inApp);
		FUNCTION_SAVECONFIG = new SaveConfig(inApp);
		FUNCTION_EDIT_WAYPOINT_NAME = new PointNameEditor(inApp);
		FUNCTION_REARRANGE_WAYPOINTS = new RearrangeWaypointsFunction(inApp);
		if( inApp instanceof PruneApp ) {
			FUNCTION_COMPRESS = new CompressTrackFunction(inApp);
		}
		FUNCTION_LOOKUP_SRTM = new LookupSrtmFunction(inApp);
		FUNCTION_ADD_TIME_OFFSET = new AddTimeOffset(inApp);
		FUNCTION_ADD_ALTITUDE_OFFSET = new AddAltitudeOffset(inApp);
		FUNCTION_CONVERT_NAMES_TO_TIMES = new ConvertNamesToTimes(inApp);
		FUNCTION_DELETE_FIELD_VALUES = new DeleteFieldValues(inApp);
		FUNCTION_PASTE_COORDINATES = new PasteCoordinates(inApp);
		FUNCTION_FIND_WAYPOINT = new FindWaypoint(inApp);
		FUNCTION_DUPLICATE_POINT = new DuplicatePoint(inApp);
		FUNCTION_CHARTS = new Charter(inApp);
		FUNCTION_DISTANCES = new DistanceFunction(inApp);
		FUNCTION_FULL_RANGE_DETAILS = new FullRangeDetails(inApp);
		FUNCTION_SET_MAP_BG = new SetMapBgFunction(inApp);
		FUNCTION_SET_DISK_CACHE = new DiskCacheConfig(inApp);
		FUNCTION_SET_PATHS = new SetPathsFunction(inApp);
		FUNCTION_SET_KMZ_IMAGE_SIZE = new SetKmzImageSize(inApp);
		FUNCTION_SET_COLOURS = new SetColours(inApp);
		FUNCTION_SET_LINE_WIDTH = new SetLineWidth(inApp);
		FUNCTION_SET_LANGUAGE = new SetLanguage(inApp);
		FUNCTION_HELP   = new HelpScreen(inApp);
		FUNCTION_SHOW_KEYS = new ShowKeysScreen(inApp);
		FUNCTION_ABOUT  = new AboutScreen(inApp);
	}
}
