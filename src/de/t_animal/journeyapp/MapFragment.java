package de.t_animal.journeyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class MapFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflat and return the layout
		View fragmentRootView = inflater.inflate(R.layout.fragment_map, container, false);

		WebView map = (WebView) fragmentRootView.findViewById(R.id.map_view);

		map.getSettings().setJavaScriptEnabled(true);
		map.loadUrl("file:///android_asset/map.html");

		return fragmentRootView;
	}

}