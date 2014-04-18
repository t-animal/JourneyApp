package de.t_animal.journeyapp;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

public class LocationService extends IntentService {

	private boolean isStopped = false;

	public LocationService() {
		super("LocationService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();

		isStopped = true;
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Intent received");

		int i = 0;

		while (!isStopped) {

			if (i++ % 10000000 == 0) {
				System.out.println("Service still running");
			}

		}
	}

}
