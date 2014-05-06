package de.t_animal.journeyapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * A foreground service that keeps requesting the location while running and sends it to a server regularly. Offers
 * location related methods like setting up geofences or getting distance to locations
 * 
 * @author ar79yxiw
 * 
 */
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
	/**
	 * Return wether the a service is running
	 * 
	 * @return true if the server is running
	 */
	static boolean isServiceRunning() {
		return singletonLocationService != null;
	}

	/**
	 * Gets the instance of the locationService if it is running, else it returns null
	 * 
	 * @return a @see LocationService or null
	 */
	static LocationService getServiceInstance() {
		return singletonLocationService;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sends a userid, latitude, longtitude and accuracy to a server (configured in res/values/strings.xml)
	 * 
	 * @param location
	 *            the location to take the values from
	 */
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

		Notification foregroundNotification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.notificationTitle))
				.setContentText(getResources().getString(R.string.notificationMessage)).build();

		startForeground(1, foregroundNotification);

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
				if (!isServiceRunning())
					break;
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
			if (Preferences.sendData(this)) {
				sendLocationToServer(curLoc);
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
