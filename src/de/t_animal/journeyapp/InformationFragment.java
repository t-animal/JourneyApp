package de.t_animal.journeyapp;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class InformationFragment extends Fragment implements OnClickListener, OnDisplayFragment {

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

	private void setButtonsFromPrefs() {
		locationServiceButton.setChecked(Preferences.sendData(this));
		info_mapFollowingUserButton.setChecked(Preferences.mapFollowsUser(this));
		info_gotCaughtButton.setChecked(Preferences.isCaught(this));
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
		setButtonsFromPrefs();
	}

	@Override
	public void onDisplay() {
		setButtonsFromPrefs();
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
		Preferences.mapFollowsUser(this, ((Switch) view).isChecked());
	}

	private void onToggleLocationService(View view) {
		if (Preferences.sendData(this)) {
			Preferences.sendData(this, false);
			getActivity().stopService(new Intent(getActivity(), LocationService.class));
		} else {
			Preferences.sendData(this, true);
			getActivity().startService(new Intent(getActivity(), LocationService.class));
		}
	}

	private void changeTheme() {
		if (Preferences.isCaught(this)) {
			Preferences.isCaught(this, false);
			((Journey) getActivity()).restartWithTheme(Journey.THEME_RUNNER);
		} else {
			Preferences.isCaught(this, true);
			((Journey) getActivity()).restartWithTheme(Journey.THEME_CHASER);
		}
	}
}
