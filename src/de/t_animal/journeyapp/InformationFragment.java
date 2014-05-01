package de.t_animal.journeyapp;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class InformationFragment extends Fragment implements OnClickListener {

	private View fragmentRootView;
	private Switch locationServiceButton;
	private Switch info_mapFollowingUserButton;
	private Switch info_gotCaughtButton;

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

		locationServiceButton = (Switch) fragmentRootView.findViewById(R.id.locationServiceButton);
		info_mapFollowingUserButton = (Switch) fragmentRootView.findViewById(R.id.info_mapFollowingUserButton);
		info_gotCaughtButton = (Switch) fragmentRootView
				.findViewById(R.id.info_gotCaughtButton);

		locationServiceButton.setOnClickListener(this);
		info_mapFollowingUserButton.setOnClickListener(this);
		info_gotCaughtButton.setOnClickListener(this);

		return fragmentRootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		locationServiceButton.setChecked(LocationService.isServiceRunning());
		info_mapFollowingUserButton.setChecked(((Journey) getActivity()).adapter.getMapFragment().co.isFollowingUser());
		info_gotCaughtButton.setChecked(((Journey) getActivity()).getCurrentJourneyTheme() != Journey.THEME_RUNNER);
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
		((Switch) view).setChecked(map.co.isFollowingUser());
	}

	private void onToggleLocationService(View view) {
		if (((Switch) view).isChecked()) {
			getActivity().startService(new Intent(getActivity(), LocationService.class));
		} else {
			getActivity().stopService(new Intent(getActivity(), LocationService.class));
		}
	}

	private void changeTheme() {
		((Journey) getActivity()).restartWithTheme(Journey.THEME_CHASER);
	}
}
