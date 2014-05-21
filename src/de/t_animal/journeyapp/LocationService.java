package de.t_animal.journeyapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.t_animal.journeyapp.containers.Zone;
import de.t_animal.journeyapp.util.JSCommunicationObject;
import de.t_animal.journeyapp.util.JourneyPreferences;
import de.t_animal.journeyapp.util.JourneyProperties;

/**
 * A foreground service that keeps requesting the location while running and sends it to a server regularly. Offers
 * location related methods like setting up geofences or getting distance to locations
 * 
 * @author ar79yxiw
 * 
 */
public class LocationService extends IntentService implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnSharedPreferenceChangeListener {

	private static final int NOTIFICATION_FOREGROUND = 1;
	private static final int NOTIFICATION_SAFEZONE = 2;

	private final String TAG = "LocationService";

	// Should be ok, because the service won't be recreated if already running
	public static LocationService singletonLocationService;

	private LocationClient locationClient;
	private Location currentLocation;
	private boolean isSafe = false;
	private long lastSafezoneCheck = 0;

	private OutputStream output;
	private OutputStream uploadOutput;

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
	public static boolean isServiceRunning() {
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

	/**
	 * Gets the last acquired location if the service is running and has acquired a location, else it returns null
	 * 
	 * @return a @see Location or null
	 */
	static Location getLastLocation() {
		if (isServiceRunning()) {
			return getServiceInstance().currentLocation;
		} else {
			return null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get a string to send to the server or save to a file containing the user's location and an id
	 * 
	 * @param location
	 * @return
	 */
	private byte[] getUserData(Location location) {
		String userId = JourneyPreferences.userID(this);
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		float acc = location.getAccuracy();
		long time = System.currentTimeMillis() / 1000;
		byte caught = JourneyPreferences.isCaught(this) ? (byte) 1 : (byte) 0;

		byte[] data = new byte[1 + userId.length() + 8 + 8 + 4 + 8 + 1];
		ByteBuffer.wrap(data).put((byte) userId.length())
				.put(userId.getBytes()).putDouble(lat).putDouble(lon)
				.putFloat(acc).putLong(time).put(caught);

		return data;
	}

	/**
	 * Sends a userid, latitude, longtitude and accuracy to a server
	 * 
	 * @param location
	 *            the location to take the values from
	 */
	private void sendLocationToServer(Location location) {
		byte[] data = getUserData(location);

		// TODO:Keep socket open and only reconnect if necessary
		DatagramSocket sock;
		try {
			sock = new DatagramSocket();

			sock.connect(new InetSocketAddress(
					JourneyProperties.getInstance(this).getServerLocation(),
					JourneyProperties.getInstance(this).getServerPort()));

			sock.send(new DatagramPacket(data, data.length));

			sock.close();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Safes the given location to a private file and (if send data is enabled) to a file for uploading later
	 * 
	 * @param location
	 */
	private void saveLocationToFiles(Location location) {
		if (output != null)
			try {
				output.write(getUserData(location));
			} catch (IOException e) {
				openOutputFile();
				Log.e(TAG, "Could not safe userlocation to SDCard", e);
			}

		if (uploadOutput != null && JourneyPreferences.sendData(this))
			try {
				uploadOutput.write(getUserData(location));
			} catch (IOException e) {
				openOutputFile();
				Log.e(TAG, "Could not safe userlocation to SDCard", e);
			}
	}

	private boolean openOutputFile() {
		try {
			output = new FileOutputStream(
					JourneyProperties.getInstance(this).getLocationFile(),
					true);
			uploadOutput = new FileOutputStream(
					JourneyProperties.getInstance(this).getUploadLocationFile(),
					true);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Directory should have been created on startup", e);
			Toast.makeText(this, "Directory structure corrupted, aborting start", Toast.LENGTH_SHORT).show();

			return false;
		}
		return true;
	}

	private void checkForSafezone() {
		if (currentLocation == null)
			return;

		boolean isNowSafe = false;

		// only check every 15 seconds
		if (System.currentTimeMillis() - lastSafezoneCheck <= 15 * 1000) {
			return;
		}
		lastSafezoneCheck = System.currentTimeMillis();

		for (Zone safeZone : JourneyProperties.getInstance(this).getSafeZones()) {
			if (safeZone.containsLocation(currentLocation)) {
				isNowSafe = true;
			}
		}

		if (isNowSafe && !isSafe) {
			Notification isSafeNotification = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_notification)
					.setContentTitle(getString(R.string.safezoneNotificationTitle))
					.setContentText(getString(R.string.safezoneNotificationText)).build();

			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(NOTIFICATION_SAFEZONE, isSafeNotification);

			((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(new long[] { 150, 150, 150, 150, 150, 150 }, -1);

			isSafe = true;
		} else if (!isNowSafe && isSafe) {
			((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(new long[] { 150, 150, 150, 150, 300, 300 }, -1);

			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.cancel(NOTIFICATION_SAFEZONE);
			isSafe = false;
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

		if (!openOutputFile()) {
			// Do not call super i.e. do not call onHandleIntent
			return IntentService.START_NOT_STICKY;
		}

		JourneyPreferences.registerOnSharedPreferenceChangeListener(this, this);
		setForegroundNotification();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		singletonLocationService = null;

		Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();

		if (locationClient != null) {
			Log.d(TAG, "disconnecting");
			locationClient.removeLocationUpdates(this);
			locationClient.disconnect();
		}

		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				Log.e(TAG, "Could not close output file", e);
			}
		}

		if (uploadOutput != null) {
			try {
				uploadOutput.close();
			} catch (IOException e) {
				Log.e(TAG, "Could not close uploadOutput file", e);
			}
		}

		new AsyncTask<Context, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Context... args) {
				Context context = args[0];

				File uploadFile = JourneyProperties.getInstance(context).getUploadLocationFile();

				if (JourneyPreferences.sendData(context) && uploadFile.length() > 0) {
					try {
						Socket uploadSocket = new Socket(JourneyProperties.getInstance(context).getServerLocation(),
								JourneyProperties.getInstance(context).getServerPort());

						OutputStream sockOutput = uploadSocket.getOutputStream();

						FileInputStream uploadFileStream = new FileInputStream(uploadFile);

						int data;
						while ((data = uploadFileStream.read()) > 0) {
							sockOutput.write(data);
						}

						sockOutput.close();
						uploadSocket.close();
						uploadFileStream.close();

					} catch (UnknownHostException e) {
						Log.e(TAG, "Could not upload data", e);
						return false;
					} catch (IOException e) {
						Log.e(TAG, "Could not upload data", e);
						return false;
					}
				}
				return true;
			}
		}.execute(this);

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2);

		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Intent received");

		while (isServiceRunning()) {
			try {
				Thread.sleep(10000);
				if (!isServiceRunning())
					break;
			} catch (InterruptedException e) {
				// do nothing, just execute as usual
			}

			Log.d(TAG, "Service still running");

			Location curLoc = null;

			// wait for the first connection
			if (locationClient.isConnected()) {
				curLoc = locationClient.getLastLocation();
				if (curLoc == null)
					continue;
			} else {
				continue;
			}

			if (JourneyPreferences.sendData(this)) {
				sendLocationToServer(curLoc);
			}

			saveLocationToFiles(curLoc);
		}
	}

	private void setForegroundNotification() {
		Intent reopenIntent = new Intent(this, Journey.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
				.addParentStack(Journey.class)
				.addNextIntent(reopenIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification foregroundNotification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(getResources().getString(R.string.notificationTitle))
				.setContentText(getResources().getString(
						JourneyPreferences.sendData(this) ? R.string.notificationMessage_sendData
								: R.string.notificationMessage_noData))
				.setContentIntent(resultPendingIntent).build();

		startForeground(NOTIFICATION_FOREGROUND, foregroundNotification);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key == JourneyPreferences.SEND_DATA) {
			setForegroundNotification();
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
	public void onLocationChanged(Location newLocation) {
		Log.d(TAG, "Location update");

		// calculate covered distance
		if (currentLocation != null) {
			float result[] = new float[1];
			Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
					newLocation.getLatitude(), newLocation.getLongitude(), result);

			JourneyPreferences.coveredDistance(this, JourneyPreferences.coveredDistance(this) + result[0]);
		}

		if (JourneyPreferences.maxSpeed(this) < newLocation.getSpeed())
			JourneyPreferences.maxSpeed(this, newLocation.getSpeed());

		checkForSafezone();

		JSCommunicationObject.getInstance(this).addWaypoint(newLocation.getLatitude(), newLocation.getLongitude());

		currentLocation = newLocation;
	}
}
