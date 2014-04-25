package de.t_animal.journeyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class MapFragment extends Fragment {

	WebView map;

	/**
	 * A class to be inserted into the JS and act as a communication bridge through it's getters and setters
	 */
	private class CommunicationObject {

		String theme;

		public void setTheme(String theme) {
			this.theme = theme;
		}

		@JavascriptInterface
		public String getTheme() {
			return this.theme;
		}
	}

	CommunicationObject co = new CommunicationObject();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflat and return the layout
		View fragmentRootView = inflater.inflate(R.layout.fragment_map, container, false);

		map = (WebView) fragmentRootView.findViewById(R.id.map_view);

		map.getSettings().setJavaScriptEnabled(true);
		map.addJavascriptInterface(co, "comm");

		map.loadUrl("file:///android_asset/map.html");

		return fragmentRootView;
	}

}