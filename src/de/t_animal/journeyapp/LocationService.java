package de.t_animal.journeyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

public class LocationService extends IntentService implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	// Should be ok, because the service won't be recreated if already running
	public static LocationService singletonLocationService;

	private LocationClient locationClient;
	private Location currentLocation;

	public LocationService() {
		super("LocationService");
		setIntentRedelivery(true);
	}

	// TODO: Make this thread-safe
	static boolean isServiceRunning() {
		return singletonLocationService != null;
	}

	static LocationService getServiceInstance() {
		return singletonLocationService;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void sendLocationToServer(Location location) {
		String userId = new String("generateAtFirstStart");
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		float acc = location.getAccuracy();

		byte[] data = new byte[1 + userId.length() + 8 + 8 + 4];
		ByteBuffer.wrap(data).put((byte) userId.length())
				.put(userId.getBytes()).putDouble(lat).putDouble(lon)
				.putFloat(acc);

		// TODO:Keep socket open and only reconnect if necessary
		DatagramSocket sock;
		try {
			sock = new DatagramSocket();

			sock.connect(new InetSocketAddress(getResources().getString(
					R.string.locationServer), 1338));

			sock.send(new DatagramPacket(data, data.length));

			sock.close();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isServiceRunning()) {
			// Do not call super i.e. do not call onHandleIntent
			return IntentService.START_NOT_STICKY;
		}
		singletonLocationService = this;

		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {
			Toast.makeText(this, R.string.noGooglePlayServices,
					Toast.LENGTH_LONG).show();

			// Do not call super i.e. do not call onHandleIntent
			return IntentService.START_NOT_STICKY;
		}

		locationClient = new LocationClient(this, this, this);
		locationClient.connect();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		singletonLocationService = null;

		Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();

		if (locationClient != null && locationClient.isConnected())
			locationClient.disconnect();

		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Intent received");

		while (isServiceRunning()) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// do nothing, just execute as usual
			}

			System.out.println("Service still running");
			Location curLoc;

			// wait for the first connection
			if (locationClient.isConnected()) {
				curLoc = locationClient.getLastLocation();
				if (curLoc == null)
					continue;
			} else {
				if (!locationClient.isConnecting())
					locationClient.connect();
				continue;
			}

			System.out.println("Current Location" + curLoc.toString());
			sendLocationToServer(curLoc);

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
		locationClient.requestLocationUpdates(
				LocationRequest.create().setInterval(5000)
						.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
				this);
	}

	@Override
	public void onDisconnected() {
		System.out.println("Disconnected from the Location Server");
	}

	@Override
	public void onLocationChanged(Location arg0) {
		currentLocation = arg0;
	}
}
