package de.t_animal.journeyapp;

import org.jraf.android.backport.switchwidget.Switch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class InformationFragment extends Fragment implements OnClickListener, OnDisplayFragment {

	private View fragmentRootView;
	private Switch locationServiceButton;
	private Switch info_mapFollowingUserButton;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_information, container, false);

		JourneyProperties prop = JourneyProperties.getInstance(getActivity());
		String journeyDateString = DateFormat.getDateFormat(getActivity()).format(prop.getJourneyDate())
				+ " " + DateFormat.getTimeFormat(getActivity()).format(prop.getJourneyDate());

		((TextView) fragmentRootView.findViewById(R.id.info_journeyName_value)).setText(prop.getJourneyName());
		((TextView) fragmentRootView.findViewById(R.id.info_journeyDate_value)).setText(journeyDateString);
		((TextView) fragmentRootView.findViewById(R.id.info_journeyLocation_value))
				.setText(prop.getJourneyStartLocation());
		((TextView) fragmentRootView.findViewById(R.id.info_journeyFurtherInformation_value))
				.setText(prop.getJourneyFurtherInformation());

		locationServiceButton = (Switch) fragmentRootView.findViewById(R.id.locationServiceButton);
		info_mapFollowingUserButton = (Switch) fragmentRootView.findViewById(R.id.info_mapFollowingUserButton);

		locationServiceButton.setOnClickListener(this);
		info_mapFollowingUserButton.setOnClickListener(this);

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

		int playTime = Preferences.playTime(this) + (int) (System.currentTimeMillis() / 1000)
				- Preferences.lastStartTime(this);
		int hours = playTime / 60 / 60;
		int minutes = playTime / 60 % 60;
		int seconds = playTime % 60;
		((TextView) fragmentRootView.findViewById(R.id.info_time_value))
				.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
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
		}
	}

	private void onToggleMapFollowingUser(View view) {
		Preferences.mapFollowsUser(this, ((Switch) view).isChecked());
	}

	private void onToggleLocationService(View view) {
		if (Preferences.sendData(this)) {
			Preferences.sendData(this, false);
		} else {
			Preferences.sendData(this, true);
		}
	}
}
