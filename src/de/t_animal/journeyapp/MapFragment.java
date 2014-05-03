package de.t_animal.journeyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MapFragment extends Fragment implements OnDisplayFragment {

	private WebView map;
	private JSCommunicationObject co;
	private View fragmentRootView;

	private class GeoWebChromeClient extends WebChromeClient {
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}
	}

	public static MapFragment newInstance() {
		MapFragment newFrag = new MapFragment();

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflat and return the layout
		fragmentRootView = inflater.inflate(R.layout.fragment_map, container, false);

		co = JSCommunicationObject.getInstance(this.getActivity());

		map = (WebView) fragmentRootView.findViewById(R.id.map_view);

		map.getSettings().setJavaScriptEnabled(true);
		map.getSettings().setGeolocationEnabled(true);
		map.setWebChromeClient(new GeoWebChromeClient());
		map.addJavascriptInterface(co, "comm");

		map.loadUrl("file:///android_asset/map.html");

		return fragmentRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		executeInMap("onResume();");
	}

	@Override
	public void onDisplay() {
		executeInMap("onDisplay();");
	}

	/**
	 * Executes the given String as javascript inside the html of the map.
	 */
	private void executeInMap(String js) {
		map.loadUrl("javascript:" + js);
	}

}