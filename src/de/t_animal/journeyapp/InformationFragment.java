package de.t_animal.journeyapp;

import org.jraf.android.backport.switchwidget.Switch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.t_animal.journeyapp.util.JourneyPreferences;
import de.t_animal.journeyapp.util.JourneyProperties;
import de.t_animal.journeyapp.util.OnDisplayFragment;

public class InformationFragment extends Fragment implements OnClickListener, OnDisplayFragment {

	private static final String TAG = "de.t_animal.journeyapp.InformationFragment";

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
		locationServiceButton.setChecked(JourneyPreferences.sendData(this));
		info_mapFollowingUserButton.setChecked(JourneyPreferences.mapFollowsUser(this));
	}

	private void updateStatistics() {
		int playTime = JourneyPreferences.playTime(this) + (int) (System.currentTimeMillis() / 1000)
				- JourneyPreferences.lastStartTime(this);
		int hours = playTime / 60 / 60;
		int minutes = playTime / 60 % 60;
		int seconds = playTime % 60;
		((TextView) fragmentRootView.findViewById(R.id.info_time_value))
				.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

		String unit = "m";
		float distance = JourneyPreferences.coveredDistance(this);
		if (distance > 10000) {
			distance /= 1000;
			unit = "km";
		}

		((TextView) fragmentRootView.findViewById(R.id.info_distance_value)).setText(
				String.format("%.2f%s", distance, unit));

		((TextView) fragmentRootView.findViewById(R.id.info_speed_max_value)).setText(
				String.format("%.2fkm/h", JourneyPreferences.maxSpeed(this) * 3.6)); // m/s=>km/h == *3.6

		if (playTime > 0) {
			double speed = JourneyPreferences.coveredDistance(this) / playTime * 3.6; // m/s=>km/h == *3.6
			((TextView) fragmentRootView.findViewById(R.id.info_speed_avg_value)).setText(
					String.format("%.2fkm/h", speed));
		} else {
			((TextView) fragmentRootView.findViewById(R.id.info_speed_avg_value)).setText(
					String.format("%.2fkm/h", 0.));
		}
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
		((Button) fragmentRootView.findViewById(R.id.info_resetJourneyButton)).setOnClickListener(this);
		((TextView) fragmentRootView.findViewById(R.id.info_flattr)).setOnClickListener(this);
		((TextView) fragmentRootView.findViewById(R.id.info_acknowledgement)).setOnClickListener(this);
		((TextView) fragmentRootView.findViewById(R.id.info_license)).setOnClickListener(this);

		return fragmentRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		setButtonsFromPrefs();
		updateStatistics();
	}

	@Override
	public void onDisplay() {
		setButtonsFromPrefs();
		updateStatistics();
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
		case R.id.info_resetJourneyButton:
			resetJourneyData();
			break;
		case R.id.info_flattr:
			String url = "https://flattr.com/submit/auto?user_id=t.animal&url=http://github.com/t-animal/JourneyApp";
			startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
			break;
		case R.id.info_acknowledgement:
		case R.id.info_license:
			showHTMLPopup(view);
			break;
		}
	}

	private void onToggleMapFollowingUser(View view) {
		JourneyPreferences.mapFollowsUser(this, ((Switch) view).isChecked());
	}

	private void onToggleLocationService(View view) {
		if (JourneyPreferences.sendData(this)) {
			JourneyPreferences.sendData(this, false);
		} else {
			JourneyPreferences.sendData(this, true);
		}
	}

	private void resetJourneyData() {

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.info_resetConfirmDialoge_title)
				.setMessage(R.string.info_resetConfirmDialoge_text)
				.setPositiveButton(R.string.info_resetConfirmDialoge_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (LocationService.isServiceRunning()) {
							Toast.makeText(getActivity(), R.string.info_resetConfirmDialoge_stopService,
									Toast.LENGTH_LONG).show();
							return;
						}

						JourneyProperties.getInstance(getActivity()).getLocationFile().delete();
						JourneyProperties.getInstance(getActivity()).getUploadLocationFile().delete();

						JourneyPreferences.resetAll(getActivity());

						updateStatistics();
					}
				})
				.setNegativeButton(R.string.info_resetConfirmDialoge_abort, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
	}

	private void showHTMLPopup(View v) {
		String title;
		String url;

		if (v.getId() == R.id.info_acknowledgement) {
			title = "Acknowledgements";
			url = "file:///android_asset/acknowledgements.html";
		} else {
			title = "License";
			url = "file:///android_asset/license.html";
		}

		WebView wv = new WebView(getActivity());
		wv.loadUrl(url);

		new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setView(wv)
				.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				}).show();

	}
}
