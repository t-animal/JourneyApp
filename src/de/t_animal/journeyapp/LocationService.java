package de.t_animal.journeyapp;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class LocationService extends IntentService {

	private boolean isStopped = false;

	public LocationService() {
		super("LocationService");
		setIntentRedelivery(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		Notification foregroundNotification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.notificationTitle))
				.setContentText(getResources().getString(R.string.notificationMessage)).build();

		startForeground(1, foregroundNotification);

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

		while (!isStopped) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// do nothing, just execute as usual
			}

			System.out.println("Service still running");

		}
	}

}
