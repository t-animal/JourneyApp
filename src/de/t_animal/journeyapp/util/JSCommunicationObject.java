package de.t_animal.journeyapp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import de.t_animal.journeyapp.LocationService;
import de.t_animal.journeyapp.containers.Coordinate;

/**
 * A class to be inserted into the JS and act as a communication bridge through it's getters and setters
 */
public class JSCommunicationObject {

	private static String TAG = "JSCommunicationObject";

	private static JSCommunicationObject singleton;
	private ArrayList<Coordinate> waypoints;

	public static JSCommunicationObject getInstance(Context context) {
		if (singleton == null) {
			singleton = new JSCommunicationObject(context);
		}
		return singleton;
	}

	private Context context;

	private JSCommunicationObject(Context context) {
		this.context = context;

		this.waypoints = new ArrayList<Coordinate>();

		InputStream file = null;
		try {
			file = new FileInputStream(JourneyProperties.getInstance(context).getLocationFile());

			// see LocationService.getUserData for how this was saved

			// skip uId string
			int i;
			while ((i = file.read()) > 0 && file.skip(i) > 0) {

				byte latData[] = new byte[8];
				byte lonData[] = new byte[8];
				file.read(latData);
				file.read(lonData);

				double lat = ByteBuffer.wrap(latData).getDouble();
				double lon = ByteBuffer.wrap(lonData).getDouble();

				addWaypoint(lat, lon);

				// skip accuracy, time and role
				file.skip(13);
			}

		} catch (FileNotFoundException e) {
			// No data was recorded yet
			return;
		} catch (IOException e) {
			Log.w(TAG, "Could not read previous movment", e);
			return;
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					Log.e(TAG, "Could not close file", e);
				}
			}
		}
	}

	@JavascriptInterface
	public String getTheme() {
		return JourneyPreferences.isCaught(context) ? "THEME_CHASER" : "THEME_RUNNER";
	}

	@JavascriptInterface
	public String getStart() {
		return JourneyProperties.getInstance(context).getStart().toString();
	}

	@JavascriptInterface
	public String getCheckpoints() {
		String s = Arrays.deepToString(JourneyProperties.getInstance(context).getCheckpoints());
		return s;
	}

	@JavascriptInterface
	public String getSafeZones() {
		return Arrays.deepToString(JourneyProperties.getInstance(context).getSafeZones());
	}

	@JavascriptInterface
	public String getOffLimitsZones() {
		return Arrays.deepToString(JourneyProperties.getInstance(context).getOffLimitsZones());
	}

	@JavascriptInterface
	public boolean isFollowingUser() {
		return JourneyPreferences.mapFollowsUser(context);
	}

	@JavascriptInterface
	public void setFollowingUser(boolean followingUser) {
		JourneyPreferences.mapFollowsUser(context, followingUser);
	}

	public void addWaypoint(double lat, double lon) {
		waypoints.add(new Coordinate(lat, lon));
	}

	@JavascriptInterface
	public String getPreviousMovement() {
		return Arrays.deepToString(waypoints.toArray(new Coordinate[0]));
	}

	@JavascriptInterface
	public boolean isServiceRunning() {
		return LocationService.isServiceRunning();
	}
}
