package de.t_animal.journeyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.app.Fragment;

class Preferences {

	private static final String PREF_NAME = "Journey";

	static final String MAP_FOLLOWS_USER = "map_follows_user";
	static final String IS_CAUGHT = "is_caught";
	static final String SEND_DATA = "send_data";
	static final String CAUGHT_COUNT = "caught_count";
	static final String VISITED_BITMASK = "visited_bitmask";
	static final String VISITED_TIMES = "visited_times";
	static final String PLAY_TIME = "play_time";
	static final String LAST_START_TIME = "last_start_time";
	static final String COVERED_DISTANCE = "covered_distance";

	private Preferences() {
		throw new AssertionError();
	}

	static private Editor getEditor(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
	}

	static private SharedPreferences getPref(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	static void registerOnSharedPreferenceChangeListener(Context context, OnSharedPreferenceChangeListener listener) {
		getPref(context).registerOnSharedPreferenceChangeListener(listener);
	}

	static void registerOnSharedPreferenceChangeListener(Fragment caller, OnSharedPreferenceChangeListener listener) {
		registerOnSharedPreferenceChangeListener(caller.getActivity(), listener);
	}

	/*
	 * mapFollowsUser
	 */

	static void mapFollowsUser(Context caller, boolean following) {
		getEditor(caller).putBoolean(MAP_FOLLOWS_USER, following).commit();
	}

	static boolean mapFollowsUser(Context caller) {
		return getPref(caller).getBoolean(MAP_FOLLOWS_USER, false);
	}

	static void mapFollowsUser(Fragment caller, boolean following) {
		mapFollowsUser(caller.getActivity(), following);
	}

	static boolean mapFollowsUser(Fragment caller) {
		return mapFollowsUser(caller.getActivity());
	}

	/*
	 * hasGotCaught
	 */
	static void isCaught(Context caller, boolean isCaught) {
		getEditor(caller).putBoolean(IS_CAUGHT, isCaught).commit();
	}

	static boolean isCaught(Context caller) {
		return getPref(caller).getBoolean(IS_CAUGHT, false);
	}

	static void isCaught(Fragment caller, boolean isCaught) {
		isCaught(caller.getActivity(), isCaught);
	}

	static boolean isCaught(Fragment caller) {
		return isCaught(caller.getActivity());
	}

	/*
	 * sendData
	 */
	static void sendData(Context caller, boolean sendData) {
		getEditor(caller).putBoolean(SEND_DATA, sendData).commit();
	}

	static boolean sendData(Context caller) {
		return getPref(caller).getBoolean(SEND_DATA, true);
	}

	static void sendData(Fragment caller, boolean sendData) {
		sendData(caller.getActivity(), sendData);
	}

	static boolean sendData(Fragment caller) {
		return sendData(caller.getActivity());
	}

	/*
	 * caughtCount
	 */
	static void caughtCount(Context caller, int count) {
		getEditor(caller).putInt(CAUGHT_COUNT, count).commit();
	}

	static int caughtCount(Context caller) {
		return getPref(caller).getInt(CAUGHT_COUNT, 0);
	}

	static void caughtCount(Fragment caller, int count) {
		caughtCount(caller.getActivity(), count);
	}

	static int caughtCount(Fragment caller) {
		return caughtCount(caller.getActivity());
	}

	/*
	 * visitedBitmask
	 */
	static void visitedBitMask(Context caller, int bitMaks) {
		getEditor(caller).putInt(VISITED_BITMASK, bitMaks).commit();
	}

	static int visitedBitMask(Context caller) {
		return getPref(caller).getInt(VISITED_BITMASK, 0);
	}

	static void visitedBitMask(Fragment caller, int bitMaks) {
		visitedBitMask(caller.getActivity(), bitMaks);
	}

	static int visitedBitMask(Fragment caller) {
		return visitedBitMask(caller.getActivity());
	}

	/*
	 * visitedTimes
	 */
	static void visitedTimes(Context caller, String timesString) {
		getEditor(caller).putString(VISITED_TIMES, timesString).commit();
	}

	static String visitedTimes(Context caller) {
		// return 32 empty entries (=32 bits in an int => max bitmask size) and one " " entry for split to work easily
		return getPref(caller).getString(VISITED_TIMES, ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, ");
	}

	static void visitedTimes(Fragment caller, String timesString) {
		visitedTimes(caller.getActivity(), timesString);
	}

	static String visitedTimes(Fragment caller) {
		return visitedTimes(caller.getActivity());
	}

	/*
	 * playTime
	 */
	static void playTime(Context caller, int playTime) {
		getEditor(caller).putInt(PLAY_TIME, playTime).commit();
	}

	static int playTime(Context caller) {
		return getPref(caller).getInt(PLAY_TIME, 0);
	}

	static void playTime(Fragment caller, int playTime) {
		playTime(caller.getActivity(), playTime);
	}

	static int playTime(Fragment caller) {
		return playTime(caller.getActivity());
	}

	/*
	 * playTime
	 */
	static void lastStartTime(Context caller, int lastStartTime) {
		if (lastStartTime == -1)
			getEditor(caller).remove(LAST_START_TIME).commit();
		else
			getEditor(caller).putInt(LAST_START_TIME, lastStartTime).commit();
	}

	static int lastStartTime(Context caller) {
		return getPref(caller).getInt(LAST_START_TIME, (int) (System.currentTimeMillis() / 1000));
	}

	static void lastStartTime(Fragment caller, int lastStartTime) {
		lastStartTime(caller.getActivity(), lastStartTime);
	}

	static int lastStartTime(Fragment caller) {
		return lastStartTime(caller.getActivity());
	}

	/*
	 * coveredDistance
	 */
	static void coveredDistance(Context caller, float coveredDistance) {
		getEditor(caller).putFloat(COVERED_DISTANCE, coveredDistance).commit();
	}

	static float coveredDistance(Context caller) {
		return getPref(caller).getFloat(COVERED_DISTANCE, 0);
	}

	static void coveredDistance(Fragment caller, float coveredDistance) {
		coveredDistance(caller.getActivity(), coveredDistance);
	}

	static float coveredDistance(Fragment caller) {
		return coveredDistance(caller.getActivity());
	}
}
