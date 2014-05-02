package de.t_animal.journeyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MapFragment extends Fragment {

	WebView map;

	JSCommunicationObject co;

	private class GeoWebChromeClient extends WebChromeClient {
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflat and return the layout
		View fragmentRootView = inflater.inflate(R.layout.fragment_map, container, false);

		co = JSCommunicationObject.getInstance(this.getActivity());

		map = (WebView) fragmentRootView.findViewById(R.id.map_view);

		map.getSettings().setJavaScriptEnabled(true);
		map.getSettings().setGeolocationEnabled(true);
		map.setWebChromeClient(new GeoWebChromeClient());
		map.addJavascriptInterface(co, "comm");

		setMapThemeValue();

		map.loadUrl("file:///android_asset/map.html");

		return fragmentRootView;
	}

	/**
	 * Sets the theme inside the map by setting it in the @see CommunicationObject , but does not trigger the update
	 * inside the js
	 */
	private void setMapThemeValue() {
		int theme = getActivity().getIntent().getIntExtra("Theme", Journey.THEME_RUNNER);

		switch (theme) {
		case Journey.THEME_CHASER:
			co.setTheme("THEME_CHASER");
			break;
		case Journey.THEME_RUNNER:
		default:
			co.setTheme("THEME_RUNNER");
			break;
		}
	}

	/**
	 * Executes the given String as javascript inside the html of the map.
	 */
	void executeInMap(String js) {
		map.loadUrl("javascript:" + js);
	}

}