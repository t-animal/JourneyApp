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
import android.widget.ListView;
import android.widget.ToggleButton;
import de.t_animal.journeyapp.CheckpointFragment.CheckpointListAdapter;

public class GameFragment extends Fragment implements OnClickListener, OnDisplayFragment {

	private final String TAG = "GameFragment";

	private View fragmentRootView;
	private ListView checkpointList;

	private ToggleButton game_gotCaughtButton;
	private ToggleButton game_startJourneyButton;

	public static GameFragment newInstance() {
		GameFragment newFrag = new GameFragment();

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_game, container, false);

		game_startJourneyButton = (ToggleButton) fragmentRootView.findViewById(R.id.game_startJourneyButton);
		game_gotCaughtButton = (ToggleButton) fragmentRootView.findViewById(R.id.game_gotCaughtButton);

		game_gotCaughtButton.setOnClickListener(this);
		game_startJourneyButton.setOnClickListener(this);

		checkpointList = (ListView) fragmentRootView.findViewById(R.id.game_checkpointsList);

		checkpointList.setAdapter(new CheckpointFragment
				.CheckpointListAdapter(getActivity(),
						JourneyProperties.getInstance(getActivity()).getCheckpoints()));

		if (LocationService.isServiceRunning()) {
			startUpdatingDistance();
		}

		return fragmentRootView;
	}

	@Override
	public void onDisplay() {
		game_startJourneyButton.setChecked(LocationService.isServiceRunning());
		game_gotCaughtButton.setChecked(Preferences.isCaught(this));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.game_gotCaughtButton:
			changeTheme();
			break;
		case R.id.game_startJourneyButton:
			toggleService();
			break;
		}
	}

	private void toggleService() {
		if (LocationService.isServiceRunning()) {
			getActivity().stopService(new Intent(getActivity(), LocationService.class));
		} else {
			getActivity().startService(new Intent(getActivity(), LocationService.class));
			startUpdatingDistance();
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
