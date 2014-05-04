package de.t_animal.journeyapp;

import org.jraf.android.backport.switchwidget.Switch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

public class GameFragment extends Fragment implements OnClickListener, OnDisplayFragment {

	private View fragmentRootView;
	private ListView checkpointList;

	private Switch game_gotCaughtButton;

	public static GameFragment newInstance() {
		GameFragment newFrag = new GameFragment();

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_game, container, false);

		game_gotCaughtButton = (Switch) fragmentRootView.findViewById(R.id.game_gotCaughtButton);
		game_gotCaughtButton.setOnClickListener(this);

		checkpointList = (ListView) fragmentRootView.findViewById(R.id.game_checkpointsList);

		checkpointList.setAdapter(new CheckpointFragment
				.CheckpointListAdapter(getActivity(),
						JourneyProperties.getInstance(getActivity()).getCheckpoints()));

		return fragmentRootView;
	}

	@Override
	public void onDisplay() {
		game_gotCaughtButton.setChecked(Preferences.isCaught(this));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.game_gotCaughtButton:
			changeTheme();
			break;
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
