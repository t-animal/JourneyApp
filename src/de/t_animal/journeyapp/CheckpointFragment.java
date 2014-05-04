package de.t_animal.journeyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CheckpointFragment extends Fragment implements OnDisplayFragment {

	private View fragmentRootView;

	public static CheckpointFragment newInstance() {
		CheckpointFragment newFrag = new CheckpointFragment();

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_checkpoint, container, false);

		return fragmentRootView;
	}

	@Override
	public void onDisplay() {
	}
}
