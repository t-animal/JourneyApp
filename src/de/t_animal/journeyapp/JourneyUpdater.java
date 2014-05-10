package de.t_animal.journeyapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.t_animal.journeyapp.util.JourneyPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class JourneyUpdater extends Activity {
	private final String TAG = "JourneyUpdater";

	ProgressBar updateProgress;
	JourneyDownloader downloader;
	Button skipButton;
	TextView explanationText;

	/**
	 * Sets the theme according to the value "Theme" passed in the start intent's extras
	 */
	private void setJourneyTheme() {
		int theme = getIntent().getIntExtra("Theme",
				JourneyPreferences.isCaught(this) ? Journey.THEME_CHASER : Journey.THEME_RUNNER);

		switch (theme) {
		case Journey.THEME_CHASER:
			setTheme(R.style.Theme_Chaser);
			break;
		case Journey.THEME_RUNNER:
		default:
			setTheme(R.style.Theme_Runner);
			break;
		}
	}

	void setExplanation(String explanation) {
		explanationText.setText(explanation);
	}

	void disableButton() {
		skipButton.setEnabled(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setJourneyTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_data);

		updateProgress = (ProgressBar) findViewById(R.id.update_progress);
		skipButton = (Button) findViewById(R.id.update_skipButton);
		explanationText = (TextView) findViewById(R.id.update_explanation);

		skipButton.setEnabled(PreferenceManager.getDefaultSharedPreferences(this).getInt("net.hawo.journey", -1) != -1);

		downloader = new JourneyDownloader(this);
		downloader.execute();
	}

	public void startMainView(View v) {
		// Not thread safe, might corrupt data :(
		if (!downloader.isFinished()) {
			downloader.cancel(true);
		}
		Intent i = new Intent(this, Journey.class);
		startActivity(i);
		finish();
	}

	private class JourneyDownloader extends AsyncTask<Void, Integer, Boolean> {

		Context context;
		boolean finished = false;

		public boolean isFinished() {
			return finished;
		}

		public JourneyDownloader(Context context) {
			this.context = context;
		}

		private boolean downloadFile(String filename) throws MalformedURLException, IOException {

			HttpURLConnection connection = (HttpURLConnection) new URL(
					"http://wwwcip.cs.fau.de/~ar79yxiw/journeyData/" + filename).openConnection();

			int fileSize = connection.getContentLength();

			PreferenceManager.getDefaultSharedPreferences(context)
					.edit().putInt("net.hawo.journey", -1).commit();

			InputStream inputStream = connection.getInputStream();
			OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
					+ "/de.t_animal/journeyApp/net.hawo.journey/" + filename);

			byte data[] = new byte[4096];
			int transferred = 0;
			int count;

			while ((count = inputStream.read(data)) != -1) {

				if (isCancelled()) {
					inputStream.close();
					return false;
				}

				transferred += count;
				if (fileSize > 0) {
					setProgress((int) (transferred * 100 / fileSize));
				}
				outputStream.write(data, 0, count);
			}

			connection.disconnect();
			inputStream.close();
			outputStream.close();

			return true;
		}

		protected Boolean doInBackground(Void... nothing) {
			InputStream inputStream;
			OutputStream outputStream;
			HttpURLConnection connection;

			try {
				URL url = new URL("http://wwwcip.cs.fau.de/~ar79yxiw/journeyData/version");
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e(TAG, "Server Error:" + connection.getResponseMessage());
					return false;
				}

				byte data[] = new byte[4096];
				int count = connection.getInputStream().read(data);
				String vString = new String(data, 0, count, "UTF-8");

				if (vString.endsWith("\n"))
					vString = vString.substring(0, vString.length() - 1);

				int curVersion = Integer.parseInt(vString);
				int oldVersion = PreferenceManager.getDefaultSharedPreferences(context).getInt("net.hawo.journey", -1);

				if (oldVersion == curVersion)
					return true;

				setProgress(-1);

				connection.disconnect();

				new File(Environment.getExternalStorageDirectory().getPath()
						+ "/de.t_animal/journeyApp/net.hawo.journey/").mkdirs();

				updateProgress.setIndeterminate(false);

				if (downloadFile("properties.xml") && downloadFile("places.kml")) {

					PreferenceManager.getDefaultSharedPreferences(context)
							.edit().putInt("net.hawo.journey", curVersion).commit();
					return true;

				} else {
					return false;
				}

			} catch (Exception e) {
				PreferenceManager.getDefaultSharedPreferences(context)
						.edit().putInt("net.hawo.journey", -1).commit();
				Log.e(TAG, "Could not download", e);
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);

			if (progress[0] == -1) {
				// do not let skip from now on
				disableButton();
				setExplanation(getString(R.string.update_downloadingFiles));
			}

			updateProgress.setMax(100);
			updateProgress.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			finished = true;
			super.onPostExecute(result);

			if (!result.booleanValue()) {
				if (PreferenceManager.getDefaultSharedPreferences(context).getInt("net.hawo.journey", -1) == -1) {
					Toast.makeText(context,
							"Could not get journey data (and/or already downloaded data was corrupted)",
							Toast.LENGTH_LONG).show();
					finish();
				}
			} else {
				startMainView(null);
			}
		}
	}
}
