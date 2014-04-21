package de.t_animal.journeyapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

public class LocationService extends IntentService implements
		ConnectionCallbacks, OnConnectionFailedListener {

	private boolean isStopped = false;
	private LocationClient locationClient;

	public LocationService() {
		super("LocationService");
		setIntentRedelivery(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {
			Toast.makeText(this, R.string.noGooglePlayServices,
					Toast.LENGTH_LONG).show();

			// TODO: Find out if this is clever ;)
			// I think this should bypass thread creation or at least
			// onHandleIntent() being called
			return IntentService.START_NOT_STICKY;
		}

		locationClient = new LocationClient(this, this, this);
		locationClient.connect();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();

		locationClient.disconnect();

		isStopped = true;
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Intent received");

		while (!isStopped) {
			System.out.println("Service still running");
			Location curLoc;
			
			//wait actively for the first connection
			if(locationClient.isConnected()){
				curLoc = locationClient.getLastLocation();
			}else{
				if(!locationClient.isConnecting())
					locationClient.connect();
				continue;
			}

			System.out.println("Current Location"+curLoc.toString());

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// do nothing, just execute as usual
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Callbacks for the Location Update Server
	 */

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, R.string.locationServerConnectionFailure,
				Toast.LENGTH_SHORT).show();
		stopSelf();
	}

	@Override
	public void onConnected(Bundle arg0) {
		System.out.println("Connected to the Location Server");
	}

	@Override
	public void onDisconnected() {
		System.out.println("Disconnected from the Location Server");
	}

}
