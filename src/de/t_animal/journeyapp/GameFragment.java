package de.t_animal.journeyapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.t_animal.journeyapp.CheckpointFragment.CheckpointListAdapter;
import de.t_animal.journeyapp.util.JourneyPreferences;
import de.t_animal.journeyapp.util.JourneyProperties;
import de.t_animal.journeyapp.util.OnDisplayFragment;

public class GameFragment extends Fragment implements OnClickListener, OnDisplayFragment {

	private final String TAG = "GameFragment";

	private View fragmentRootView;
	private ListView checkpointList;
	private TextView game_othersCaught_value;

	private Button game_gotCaughtButton;
	private Button game_notGotCaughtButton;
	private ToggleButton game_startJourneyButton;
	private Button game_othersCaught_plus;
	private Button game_othersCaught_minus;

	public static GameFragment newInstance() {
		GameFragment newFrag = new GameFragment();

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_game, container, false);

		game_startJourneyButton = (ToggleButton) fragmentRootView.findViewById(R.id.game_startJourneyButton);
		game_gotCaughtButton = (Button) fragmentRootView.findViewById(R.id.game_gotCaughtButton);
		game_notGotCaughtButton = (Button) fragmentRootView.findViewById(R.id.game_notGotCaughtButton);
		game_othersCaught_value = (TextView) fragmentRootView.findViewById(R.id.game_othersCaught_count);
		game_othersCaught_plus = (Button) fragmentRootView.findViewById(R.id.game_othersCaught_plus);
		game_othersCaught_minus = (Button) fragmentRootView.findViewById(R.id.game_othersCaught_minus);

		game_gotCaughtButton.setOnClickListener(this);
		game_startJourneyButton.setOnClickListener(this);
		game_notGotCaughtButton.setOnClickListener(this);
		game_othersCaught_plus.setOnClickListener(this);
		game_othersCaught_minus.setOnClickListener(this);

		checkpointList = (ListView) fragmentRootView.findViewById(R.id.game_checkpointsList);

		checkpointList.setAdapter(new CheckpointFragment
				.CheckpointListAdapter(getActivity(),
						JourneyProperties.getInstance(getActivity()).getCheckpoints()));
		checkpointList.setOnItemClickListener((OnItemClickListener) checkpointList.getAdapter());

		if (LocationService.isServiceRunning()) {
			startUpdatingDistance();
		}

		return fragmentRootView;
	}

	@Override
	public void onDisplay() {
		game_startJourneyButton.setChecked(LocationService.isServiceRunning());
		game_othersCaught_value.setText("" + JourneyPreferences.caughtCount(this));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.game_gotCaughtButton:
		case R.id.game_notGotCaughtButton:
			changeTheme();
			break;
		case R.id.game_startJourneyButton:
			toggleGameRunning();
			break;
		case R.id.game_othersCaught_plus:
			caughtMore();
			break;
		case R.id.game_othersCaught_minus:
			caughtLess();
			break;
		}
	}

	private void caughtLess() {
		int newVal = JourneyPreferences.caughtCount(this) - 1;
		JourneyPreferences.caughtCount(this, newVal < 0 ? 0 : newVal);
		game_othersCaught_value.setText("" + JourneyPreferences.caughtCount(this));
	}

	private void caughtMore() {
		JourneyPreferences.caughtCount(this, JourneyPreferences.caughtCount(this) + 1);
		game_othersCaught_value.setText("" + JourneyPreferences.caughtCount(this));
	}

	private void toggleGameRunning() {
		if (LocationService.isServiceRunning()) {

			getActivity().stopService(new Intent(getActivity(), LocationService.class));

			long endTime = System.currentTimeMillis() / 1000;
			JourneyPreferences.playTime(this,
					(int) (JourneyPreferences.playTime(this) + endTime - JourneyPreferences.lastStartTime(this)));
			JourneyPreferences.lastStartTime(this, -1);

		} else {

			getActivity().startService(new Intent(getActivity(), LocationService.class));
			startUpdatingDistance();

			JourneyPreferences.lastStartTime(this, (int) (System.currentTimeMillis() / 1000));
		}
	}

	private void changeTheme() {
		if (JourneyPreferences.isCaught(this)) {
			JourneyPreferences.isCaught(this, false);
			((Journey) getActivity()).restartWithTheme(Journey.THEME_RUNNER);
		} else {
			JourneyPreferences.isCaught(this, true);
			((Journey) getActivity()).restartWithTheme(Journey.THEME_CHASER);
		}
	}

	private void startUpdatingDistance() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				if (LocationService.isServiceRunning()) {
					Location curLoc = LocationService.getLastLocation();
					if (curLoc != null) {
						((CheckpointListAdapter) checkpointList.getAdapter()).updateLocation();
						((CheckpointListAdapter) checkpointList.getAdapter()).notifyDataSetChanged();
					}

					handler.postDelayed(this, 1000);
				} else {
					((CheckpointListAdapter) checkpointList.getAdapter()).updateLocation();
					((CheckpointListAdapter) checkpointList.getAdapter()).notifyDataSetChanged();
					handler.removeCallbacks(this);
				}
			}
		};

		// 500: give service time to start
		handler.postDelayed(runnable, 500);
	}

}
