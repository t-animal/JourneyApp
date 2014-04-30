package de.t_animal.journeyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

public class InformationFragment extends Fragment implements OnClickListener {

	private View fragmentRootView;

	public static InformationFragment newInstance() {
		InformationFragment newFrag = new InformationFragment();

		/*
		 * Bundle args = new Bundle(); args.putInt("id", number); newFrag.setArguments(args);
		 */

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_information, container, false);

		fragmentRootView.findViewById(R.id.locationServiceButton).setOnClickListener(this);
		fragmentRootView.findViewById(R.id.info_mapFollowingUserButton).setOnClickListener(this);
		fragmentRootView.findViewById(R.id.info_gotCaughtButton).setOnClickListener(this);

		return fragmentRootView;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.locationServiceButton:
			onToggleLocationService(view);
			break;
		case R.id.info_mapFollowingUserButton:
			onToggleMapFollowingUser(view);
			break;
		case R.id.info_gotCaughtButton:
			changeTheme();
			break;
		}
	}

	private void onToggleMapFollowingUser(View view) {
		MapFragment map = ((Journey) getActivity()).adapter.getMapFragment();
		map.executeInMap("toggleLocationChangedListener();");
		((ToggleButton) view).setChecked(map.co.isFollowingUser());
	}

	private void onToggleLocationService(View view) {
		if (((ToggleButton) view).isChecked()) {
			getActivity().startService(new Intent(getActivity(), LocationService.class));
		} else {
			getActivity().stopService(new Intent(getActivity(), LocationService.class));
		}
	}

	private void changeTheme() {
		((Journey) getActivity()).restartWithTheme(Journey.THEME_CHASER);
	}
}
