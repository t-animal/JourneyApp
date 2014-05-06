package de.t_animal.journeyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.Fragment;

class Preferences {

	private static final String PREF_NAME = "Journey";

	private static final String MAP_FOLLOWS_USER = "map_follows_user";
	private static final String IS_CAUGHT = "is_caught";
	private static final String SEND_DATA = "send_data";

	private Preferences() {
		throw new AssertionError();
	}

	static private Editor getEditor(Context context) {
		return context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE).edit();
	}

	static private SharedPreferences getPref(Context context) {
		return context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
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
}
