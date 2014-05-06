package de.t_animal.journeyapp;

import java.util.Calendar;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import de.t_animal.journeyapp.JourneyProperties.Checkpoint;

public class CheckpointFragment extends Fragment implements OnDisplayFragment {

	private final static String TAG = "CheckpointFragment";

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
		((TextView) fragmentRootView.findViewById(R.id.checkpoint_description_value)).setText(args
				.getString("description"));

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
	public static class CheckpointListAdapter extends ArrayAdapter<Checkpoint> implements OnItemClickListener {
		private Checkpoint[] checkpoints;
		private Location currentLocation;

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
			((TextView) convertView.findViewById(R.id.checkpoint_description_value))
					.setText(checkpoints[i].description);
			if (checkpoints[i].safeZone != null)
				((TextView) convertView.findViewById(R.id.checkpoint_safezoneDescription_value))
						.setText(checkpoints[i].safeZone.description);

			if (currentLocation != null) {
				float results[] = new float[1];
				String unit = "m";

				Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
						checkpoints[i].lat, checkpoints[i].lon, results);

				if (results[0] > 1000) {
					results[0] /= 1000;
					unit = "km";
				}

				((TextView) convertView.findViewById(R.id.checkpoint_distance_value)).setText(
						String.format("%.2f%s", results[0], unit));
			} else {
				((TextView) convertView.findViewById(R.id.checkpoint_distance_value)).setText("--");
			}

			CheckBox visitedButton = ((CheckBox) convertView.findViewById(R.id.checkpoint_visitedButton));
			TextView visitedTime = (TextView) convertView.findViewById(R.id.checkpoint_visited_value);
			if ((Preferences.visitedBitMask(context) & (0x01 << i)) > 0) {
				visitedButton.setChecked(true);
				visitedTime.setText(Preferences.visitedTimes(context).split(",")[i]);
			} else {
				visitedButton.setChecked(false);
				visitedTime.setText("");
			}

			return convertView;
		}

		public void updateLocation() {
			currentLocation = LocationService.getLastLocation();
		}

		@Override
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			CheckBox cb = (CheckBox) v.findViewById(R.id.checkpoint_visitedButton);
			cb.setChecked(!cb.isChecked());

			String times[] = Preferences.visitedTimes(context).split(",");
			if (cb.isChecked()) {
				times[position] = (String) DateFormat.format("HH:mm", Calendar.getInstance().getTime());
				Preferences.visitedBitMask(context, (Preferences.visitedBitMask(context) | (1 << position)));
			} else {
				times[position] = "";
				Preferences.visitedBitMask(context, (Preferences.visitedBitMask(context) & ~(1 << position)));
			}
			Preferences.visitedTimes(context, TextUtils.join(",", times));

			((TextView) v.findViewById(R.id.checkpoint_visited_value)).setText(times[position]);
		}
	}
}
