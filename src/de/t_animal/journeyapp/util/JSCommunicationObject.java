package de.t_animal.journeyapp.util;

import java.util.Arrays;


import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * A class to be inserted into the JS and act as a communication bridge through it's getters and setters
 */
public class JSCommunicationObject {

	private static JSCommunicationObject singleton;

	public static JSCommunicationObject getInstance(Context context) {
		if (singleton == null) {
			singleton = new JSCommunicationObject(context);
		}
		return singleton;
	}

	private Context context;

	private JSCommunicationObject(Context context) {
		this.context = context;
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
}
