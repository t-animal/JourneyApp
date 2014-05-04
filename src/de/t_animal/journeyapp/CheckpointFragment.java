package de.t_animal.journeyapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.t_animal.journeyapp.JourneyProperties.Checkpoint;

public class CheckpointFragment extends Fragment implements OnDisplayFragment {

	private View fragmentRootView;

	public static CheckpointFragment newInstance(JourneyProperties.Checkpoint checkpoint) {
		CheckpointFragment newFrag = new CheckpointFragment();

		Bundle bundle = new Bundle();
		bundle.putString("name", checkpoint.name);
		bundle.putString("description", checkpoint.description);
		bundle.putDouble("lat", checkpoint.lat);
		bundle.putDouble("lon", checkpoint.lon);

		newFrag.setArguments(bundle);

		return newFrag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentRootView = inflater.inflate(R.layout.fragment_checkpoint, container, false);

		Bundle args = getArguments();

		((TextView) fragmentRootView.findViewById(R.id.checkpoint_name)).setText(args.getString("name"));
		((TextView) fragmentRootView.findViewById(R.id.checkpoint_description)).setText(args.getString("description"));

		return fragmentRootView;
	}

	@Override
	public void onDisplay() {
	}

	/**
	 * An adapter that can be used to fill a list with CheckpointFragments
	 * 
	 * @author ar79yxiw
	 * 
	 */
	public static class CheckpointListAdapter extends ArrayAdapter<Checkpoint> {
		private Checkpoint[] checkpoints;

		private LayoutInflater inflater;
		private Context context;

		public CheckpointListAdapter(Context context, Checkpoint[] checkpoints) {
			super(context, R.layout.fragment_checkpoint, checkpoints);

			this.checkpoints = checkpoints;
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		public View getView(int i, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.fragment_checkpoint, parent, false);
			}

			// Uhoh... what if convertView does not have these things?!
			((TextView) convertView.findViewById(R.id.checkpoint_name)).setText(checkpoints[i].name);
			((TextView) convertView.findViewById(R.id.checkpoint_description)).setText(checkpoints[i].description);

			return convertView;
		}
	}
}
