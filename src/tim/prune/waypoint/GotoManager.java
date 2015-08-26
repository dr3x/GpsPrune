package tim.prune.waypoint;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import tim.prune.App;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.Altitude.Format;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.MapPosition;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

public class GotoManager implements ComboBoxModel {
	public static abstract class GotoListener implements ListDataListener {
		// We only use contentsChanged, so this class can shortcut
		// creating listeners for GotoManager.
		@Override
		abstract public void contentsChanged(ListDataEvent event);
		@Override
		public void intervalAdded(ListDataEvent event) {
		}
		@Override
		public void intervalRemoved(ListDataEvent event) {
		}
	}

	private static GotoManager _gotoManager = null;

	private List<DataPoint> _userWaypoints = null;
	private List<DataPoint> _waypoints = null;
	private List<ListDataListener> _listeners = null;
	private App _app = null;
	private GotoDialog _gridDialog = null;
	private GotoMenu _menu = null;
	private Object _selectedItem = null;

	private void loadWaypoints(final String inFileName, 
			final List<DataPoint> inList, boolean checkClasspath) {
		String path = Config.getConfigString(Config.KEY_TILE_LOCATION);
		File f = new File(path+"/"+inFileName);
		InputStream defIS = null;
		if (!f.exists() && checkClasspath) {
			defIS = GotoManager.class.getResourceAsStream("/"+inFileName);
			if( defIS == null ) {
				Logger.getLogger(GotoManager.class.getName()).log(Level.WARNING, "Failed to load "+inFileName+" from jar.");
			}
		} else {
			try {
				defIS = new FileInputStream(f);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(GotoManager.class.getName()).log(Level.WARNING, "Failed to load "+inFileName+".");
			}
		}
		if (defIS == null) return;
		DataInputStream in = null;
		try {
			in = new DataInputStream(defIS);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] vals = strLine.split(",", 4);
				if (vals.length == 4) {
					Coordinate lat = new Latitude(Double.parseDouble(vals[1]), Coordinate.FORMAT_NONE);
					Coordinate lon = new Longitude(Double.parseDouble(vals[0]), Coordinate.FORMAT_NONE);
					Altitude zoom = new Altitude(Integer.parseInt(vals[2]), Format.NO_FORMAT);
					DataPoint dp = new DataPoint(lat, lon, zoom);
					dp.setFieldValue(Field.WAYPT_NAME, vals[3], false);
					inList.add(dp);
				}
			}
		} catch (IOException e) { 
		} finally {
			if (in != null) try { in.close(); } catch (IOException e) { }
		}
	}

	protected GotoManager(App inApp, JFrame inParentFrame) {
		_userWaypoints = new LinkedList<DataPoint>();
		_waypoints = new LinkedList<DataPoint>();
		_listeners = new LinkedList<ListDataListener>();
		_app = inApp;

		loadWaypoints("default_waypoints.txt", _waypoints, true);
		loadWaypoints("user_waypoints.txt", _userWaypoints, false);
		_gridDialog = new GotoDialog(this, inParentFrame);
		_menu = new GotoMenu(this);		
		_menu.enableStartView();
	}

	private void dataChanged() {
		ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
		for (ListDataListener l : _listeners) {
			l.contentsChanged(ev);
		}
	}

	public static void initialize(App inApp, JFrame inParentFrame) {
		_gotoManager = new GotoManager(inApp, inParentFrame);
	}

	public static GotoManager getInstance() {
		return _gotoManager;
	}

	protected void gotoWaypoint(double inLat, double inLon, int inZoom) {		
		if (inZoom != 0) {
			_app.getViewport().getMapCanvas().setZoom(inZoom);
		} else {
			if (_app.getViewport().getMapCanvas().getZoom() < 5) {
				_app.getViewport().getMapCanvas().setZoom(5);
			}
		}
		_app.getViewport().getMapCanvas().gotoLatLon(inLat, inLon);
	}

	/**
	 * Go to the specified points coordinates
	 * @param inWaypointIndex index of selected waypoint
	 */
	public void gotoWaypoint(int inIndex)
	{
		DataPoint p = getWaypoint(inIndex);
		if (p != null) {
			gotoWaypoint(p.getLatitude().getDouble(), 
					p.getLongitude().getDouble(), p.getAltitude().getValue());
		}
	}

	/**
	 * @return Returns the number of user waypoints.
	 */
	public int getUserSize()
	{
		return _userWaypoints.size();
	}

	/**
	 * @return The number of waypoints
	 */
	public int getSize()
	{
		return _userWaypoints.size() + _waypoints.size();
	}

	/**
	 * @param inIndex index number, starting at 0
	 * @returns String for the waypoint name at index inIndex
	 */
	public Object getElementAt(int inIndex)
	{
		DataPoint p = getWaypoint(inIndex);
		return p != null?p.getWaypointName():"";
	}

	/**
	 * Get the waypoint at the given index
	 * @param inIndex index number, starting at 0
	 * @return DataPoint object
	 */
	public DataPoint getWaypoint(int inIndex)
	{
		DataPoint p = null;

		if (inIndex >= 0 && inIndex < (_userWaypoints.size() + _waypoints.size())) {
			if (inIndex < _userWaypoints.size()) {
				p = _userWaypoints.get(inIndex);
			} else {
				p = _waypoints.get(inIndex - _userWaypoints.size());
			}
		}

		return p;
	}

	public boolean isDialogOpen() {
		return _gridDialog.isVisible();
	}

	public void openDialog() {
		_gridDialog.setRestorePoint();
		_gridDialog.setVisible(true);
	}

	public void setCurrentLatLon(double inLat, double inLon) {
		LatLonPoint llp = new LatLonPoint(inLat, inLon);
		MGRSPoint mgrsPoint = new MGRSPoint();
		MGRSPoint.LLtoMGRS(llp, mgrsPoint);
		_gridDialog.setPoint(mgrsPoint);
		_gridDialog.enableSave();
	}

	private void saveUserWaypoints() {
		String path = Config.getConfigString(Config.KEY_TILE_LOCATION);
		File pf = new File(path);
		if (pf.exists() && pf.isDirectory()) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(path
						+ "/user_waypoints.txt"));
				for (DataPoint p : _userWaypoints) {
					out.println(p.getLongitude().getDouble() + ","
					+ p.getLatitude().getDouble() + ","+
					+ p.getAltitude().getValue()+","+
					p.getWaypointName());
				}
			} catch (IOException ex) { 
			} finally {
				if (out != null) out.close();
			}
		}
	}

	private void addUserWaypoint(String inName, double inLat, double inLon) {
		if ((inName != null) && (!inName.equals(""))) {
			Coordinate lat = new Latitude(inLat, Coordinate.FORMAT_NONE);
			Coordinate lon = new Longitude(inLon, Coordinate.FORMAT_NONE);
			DataPoint p = null;
			for (DataPoint dp : _userWaypoints) {
				if (dp.getWaypointName().equals(inName)) {
					p = dp;
					break;
				}
			}
			if (p == null) {
				p = new DataPoint(lat, lon, null);
				p.setFieldValue(Field.WAYPT_NAME, inName, false);
				_userWaypoints.add(p);
			}
			p.setFieldValue(Field.ALTITUDE, ""+_app.getViewport().getMapCanvas().getZoom(), false);
			saveUserWaypoints();
			// Select in the drop down.
			_selectedItem = inName;
			dataChanged();
		}
	}
	
	public void saveLastPoint() {
		MapCanvas canvas = _app.getCanvas();
		MapPosition mapPosition = canvas.getMapPosition();
		double[] ll = _app.getViewport().getCenterLatLon();			
		int zoom = mapPosition.getZoom();
		Config.setConfigDouble(Config.KEY_LAST_CENTER_LAT, ll[0]);
		Config.setConfigDouble(Config.KEY_LAST_CENTER_LON, ll[1]);
		Config.setConfigInt(Config.KEY_LAST_CENTER_ZOOM, zoom);
	}

	private void saveStartPoint(String name, double lat, double lon, int zoom) {
		Config.setConfigString(Config.KEY_START_CENTER_NAME, name);
		Config.setConfigDouble(Config.KEY_START_CENTER_LAT, lat);
		Config.setConfigDouble(Config.KEY_START_CENTER_LON, lon);
		Config.setConfigInt(Config.KEY_START_CENTER_ZOOM, zoom);
	}
	
	private void deleteStartPoint() {
		Config.setConfigString(Config.KEY_START_CENTER_NAME, null);
		Config.setConfigString(Config.KEY_START_CENTER_LAT, null);
		Config.setConfigString(Config.KEY_START_CENTER_LON, null);
		Config.setConfigString(Config.KEY_START_CENTER_ZOOM, null);
	}

	public void clearSelected() {
		_selectedItem = null;
		dataChanged();  // Need this to clear the drop down.
	}

	public void save(String inName, MGRSPoint inPoint) {
		// Need a name to save it.
		if (inName == null || inName.equals("")) return;
		LatLonPoint llp = inPoint.toLatLonPoint();
		_app.getViewport().getMapCanvas().gotoLatLon(
				llp.getLatitude(),
				llp.getLongitude());
		addUserWaypoint(inName, llp.getLatitude(), llp.getLongitude());
		if (_gridDialog.isStartView()) {
			saveStartPoint(inName, llp.getLatitude(), llp.getLongitude(), _app.getCanvas().getMapPosition().getZoom());
		} else {
			String startName = Config.getConfigString(Config.KEY_START_CENTER_NAME);
			if( startName != null && startName.equals(inName) ) {
				deleteStartPoint();
			}
		}
	}

	public void gotoCoordEntry(MGRSPoint inPoint) {
		LatLonPoint llp = inPoint.toLatLonPoint();
		gotoWaypoint(llp.getLatitude(), llp.getLongitude(), 0);
	}

	public void deleteSelected() {
		if (_selectedItem != null) {
			int i = -1;
			for (DataPoint dp : _userWaypoints) {
				i++;
				if (dp.getWaypointName().equals((String)_selectedItem)) {
					break;
				}
			}
			if (i != -1) {
				_userWaypoints.remove(i);
				saveUserWaypoints();
				_gridDialog.clearDialog();
			}
		}
	}

	public boolean gotoStartView() {
		boolean ret = false;
		double lat = Config.getConfigDouble(Config.KEY_START_CENTER_LAT, Config.getConfigDouble(Config.KEY_LAST_CENTER_LAT, Double.MAX_VALUE));
		double lon = Config.getConfigDouble(Config.KEY_START_CENTER_LON, Config.getConfigDouble(Config.KEY_LAST_CENTER_LON, Double.MAX_VALUE));		
		int zoom = Config.getConfigInt(Config.KEY_START_CENTER_ZOOM, Config.getConfigInt(Config.KEY_LAST_CENTER_ZOOM, 0));
		if( lat != Double.MAX_VALUE && lon != Double.MAX_VALUE && zoom != 0 ) {
			gotoWaypoint(lat, lon, zoom);
			ret = true;
		}
		return ret;
	}

	public JMenu getMenu() {
		return _menu;
	}

	@Override
	public void addListDataListener(ListDataListener inListener) {
		_listeners.add(inListener);
	}

	@Override
	public void removeListDataListener(ListDataListener inListener) {
		_listeners.remove(inListener);
	}

	@Override
	public Object getSelectedItem() {
		return _selectedItem;
	}

	@Override
	public void setSelectedItem(Object inItem) {
		String startName = Config.getConfigString(Config.KEY_START_CENTER_NAME);		
		_selectedItem = inItem;
		for (int x = 0; x < getSize(); x++) {
			DataPoint p = getWaypoint(x);
			if (inItem.equals(p.getWaypointName())) {
				LatLonPoint llp = new LatLonPoint(p.getLatitude().getDouble(),
						p.getLongitude().getDouble());
				MGRSPoint mgrsPoint = new MGRSPoint();
				MGRSPoint.LLtoMGRS(llp, mgrsPoint);
				_gridDialog.setPoint(mgrsPoint);
				_gridDialog.setName((String)inItem);
				gotoWaypoint(p.getLatitude().getDouble(), 
						p.getLongitude().getDouble(), p.getAltitude().getValue());
				if (x < _userWaypoints.size()) _gridDialog.enableDelete();
				else _gridDialog.disableDelete();
				if (startName != null && startName.equals(inItem)) {
					_gridDialog.startViewSelected(true);
				} else {
					_gridDialog.startViewSelected(false);
				}
				_gridDialog.setRestorePoint();
			}
		}
	}
}
