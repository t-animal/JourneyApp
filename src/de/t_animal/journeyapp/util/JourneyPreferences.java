package de.t_animal.journeyapp.util;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.Fragment;

public class JourneyPreferences {

	private static final String PREF_NAME = "Journey";

	public static final String MAP_FOLLOWS_USER = "map_follows_user";
	public static final String IS_CAUGHT = "is_caught";
	public static final String SEND_DATA = "send_data";
	public static final String CAUGHT_COUNT = "caught_count";
	public static final String VISITED_BITMASK = "visited_bitmask";
	public static final String VISITED_TIMES = "visited_times";
	public static final String PLAY_TIME = "play_time";
	public static final String LAST_START_TIME = "last_start_time";
	public static final String COVERED_DISTANCE = "covered_distance";
	public static final String USER_ID = "user_id";

	private JourneyPreferences() {
		throw new AssertionError();
	}

	static private Editor getEditor(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
	}

	static private SharedPreferences getPref(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public static void registerOnSharedPreferenceChangeListener(Context context,
			OnSharedPreferenceChangeListener listener) {
		getPref(context).registerOnSharedPreferenceChangeListener(listener);
	}

	public static void registerOnSharedPreferenceChangeListener(Fragment caller,
			OnSharedPreferenceChangeListener listener) {
		registerOnSharedPreferenceChangeListener(caller.getActivity(), listener);
	}

	public static void resetAll(Context caller) {
		getEditor(caller).remove(MAP_FOLLOWS_USER).remove(IS_CAUGHT).remove(SEND_DATA).remove(CAUGHT_COUNT)
				.remove(VISITED_BITMASK).remove(VISITED_TIMES).remove(PLAY_TIME).remove(LAST_START_TIME)
				.remove(COVERED_DISTANCE).remove(USER_ID).commit();
	}

	public static void resetAll(Fragment caller) {
		resetAll(caller.getActivity());
	}

	/*
	 * mapFollowsUser
	 */

	public static void mapFollowsUser(Context caller, boolean following) {
		getEditor(caller).putBoolean(MAP_FOLLOWS_USER, following).commit();
	}

	public static boolean mapFollowsUser(Context caller) {
		return getPref(caller).getBoolean(MAP_FOLLOWS_USER, false);
	}

	public static void mapFollowsUser(Fragment caller, boolean following) {
		mapFollowsUser(caller.getActivity(), following);
	}

	public static boolean mapFollowsUser(Fragment caller) {
		return mapFollowsUser(caller.getActivity());
	}

	/*
	 * hasGotCaught
	 */
	public static void isCaught(Context caller, boolean isCaught) {
		getEditor(caller).putBoolean(IS_CAUGHT, isCaught).commit();
	}

	public static boolean isCaught(Context caller) {
		return getPref(caller).getBoolean(IS_CAUGHT, false);
	}

	public static void isCaught(Fragment caller, boolean isCaught) {
		isCaught(caller.getActivity(), isCaught);
	}

	public static boolean isCaught(Fragment caller) {
		return isCaught(caller.getActivity());
	}

	/*
	 * sendData
	 */
	public static void sendData(Context caller, boolean sendData) {
		getEditor(caller).putBoolean(SEND_DATA, sendData).commit();
	}

	public static boolean sendData(Context caller) {
		return getPref(caller).getBoolean(SEND_DATA, true);
	}

	public static void sendData(Fragment caller, boolean sendData) {
		sendData(caller.getActivity(), sendData);
	}

	public static boolean sendData(Fragment caller) {
		return sendData(caller.getActivity());
	}

	/*
	 * caughtCount
	 */
	public static void caughtCount(Context caller, int count) {
		getEditor(caller).putInt(CAUGHT_COUNT, count).commit();
	}

	public static int caughtCount(Context caller) {
		return getPref(caller).getInt(CAUGHT_COUNT, 0);
	}

	public static void caughtCount(Fragment caller, int count) {
		caughtCount(caller.getActivity(), count);
	}

	public static int caughtCount(Fragment caller) {
		return caughtCount(caller.getActivity());
	}

	/*
	 * visitedBitmask
	 */
	public static void visitedBitMask(Context caller, int bitMaks) {
		getEditor(caller).putInt(VISITED_BITMASK, bitMaks).commit();
	}

	public static int visitedBitMask(Context caller) {
		return getPref(caller).getInt(VISITED_BITMASK, 0);
	}

	public static void visitedBitMask(Fragment caller, int bitMaks) {
		visitedBitMask(caller.getActivity(), bitMaks);
	}

	public static int visitedBitMask(Fragment caller) {
		return visitedBitMask(caller.getActivity());
	}

	/*
	 * visitedTimes
	 */
	public static void visitedTimes(Context caller, String timesString) {
		getEditor(caller).putString(VISITED_TIMES, timesString).commit();
	}

	public static String visitedTimes(Context caller) {
		// return 32 empty entries (=32 bits in an int => max bitmask size) and one " " entry for split to work easily
		return getPref(caller).getString(VISITED_TIMES, ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, ");
	}

	public static void visitedTimes(Fragment caller, String timesString) {
		visitedTimes(caller.getActivity(), timesString);
	}

	public static String visitedTimes(Fragment caller) {
		return visitedTimes(caller.getActivity());
	}

	/*
	 * playTime
	 */
	public static void playTime(Context caller, int playTime) {
		getEditor(caller).putInt(PLAY_TIME, playTime).commit();
	}

	public static int playTime(Context caller) {
		return getPref(caller).getInt(PLAY_TIME, 0);
	}

	public static void playTime(Fragment caller, int playTime) {
		playTime(caller.getActivity(), playTime);
	}

	public static int playTime(Fragment caller) {
		return playTime(caller.getActivity());
	}

	/*
	 * playTime
	 */
	public static void lastStartTime(Context caller, int lastStartTime) {
		if (lastStartTime == -1)
			getEditor(caller).remove(LAST_START_TIME).commit();
		else
			getEditor(caller).putInt(LAST_START_TIME, lastStartTime).commit();
	}

	public static int lastStartTime(Context caller) {
		return getPref(caller).getInt(LAST_START_TIME, (int) (System.currentTimeMillis() / 1000));
	}

	public static void lastStartTime(Fragment caller, int lastStartTime) {
		lastStartTime(caller.getActivity(), lastStartTime);
	}

	public static int lastStartTime(Fragment caller) {
		return lastStartTime(caller.getActivity());
	}

	/*
	 * coveredDistance
	 */
	public static void coveredDistance(Context caller, float coveredDistance) {
		getEditor(caller).putFloat(COVERED_DISTANCE, coveredDistance).commit();
	}

	public static float coveredDistance(Context caller) {
		return getPref(caller).getFloat(COVERED_DISTANCE, 0);
	}

	public static void coveredDistance(Fragment caller, float coveredDistance) {
		coveredDistance(caller.getActivity(), coveredDistance);
	}

	public static float coveredDistance(Fragment caller) {
		return coveredDistance(caller.getActivity());
	}

	/*
	 * coveredDistance
	 */

	public static String userID(Context caller) {
		String uId = getPref(caller).getString(USER_ID, "");
		if (uId.length() == 0) {
			uId = UUID.randomUUID().toString();
			getEditor(caller).putString(USER_ID, uId);
		}
		return uId;
	}

	public static String userID(Fragment caller) {
		return userID(caller.getActivity());
	}
}
