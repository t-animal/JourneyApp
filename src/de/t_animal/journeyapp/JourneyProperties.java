package de.t_animal.journeyapp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class JourneyProperties {

	private static JourneyProperties singleton;
	private Properties prop;
	private Date journeyDate;
	private String[] checkpointNames;

	public static JourneyProperties getInstance(Context context) {
		if (singleton == null) {
			singleton = new JourneyProperties(context);
		}

		return singleton;
	}

	public JourneyProperties(Context context) {
		prop = new java.util.Properties();

		try {
			prop.loadFromXML(context.getAssets().open("properties.xml"));
		} catch (InvalidPropertiesFormatException e) {
			Toast.makeText(context, "Could not load properties", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Could not load properties", e);
			System.exit(0);
		} catch (IOException e) {
			Toast.makeText(context, "Could not load properties", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Could not load properties", e);
			System.exit(0);
		}

		try {
			journeyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(prop.getProperty("journeyDate"));
		} catch (ParseException e) {
			Log.e(this.getClass().getName(), "Could parse time", e);
			journeyDate = new Date(9999, 12, 31);
		}

		int checkpoints = Integer.parseInt(prop.getProperty("checkpointsCount", "0"));
		checkpointNames = new String[checkpoints];

		for (int i = 0; i < checkpoints; i++) {
			checkpointNames[i] = prop.getProperty("checkpoint" + (i + 1));

			if (checkpointNames[i] == null) {

				// Hrmpf, no arraycopy in SDK<9
				String tmp[] = new String[i];
				for (int j = 0; j < i; j++) {
					tmp[j] = checkpointNames[j];
				}
				checkpointNames = tmp;

				break;
			}
		}
	}

	public Date getJourneyDate() {
		return journeyDate;
	}

	public String getJourneyName() {
		return prop.getProperty("journeyName", "unknown");
	}

	public String getJourneyStartLocation() {
		return prop.getProperty("journeyStartLocation", "unknown");
	}

	public String getJourneyFurtherInformation() {
		return prop.getProperty("journeyFurtherInformation", "No further information.");
	}

	public String getJourneyFurtherInformationSecret() {
		return prop.getProperty("journeyFurtherInformationSecret", "");
	}

	public String[] getCheckpointNames() {
		return checkpointNames;
	}

}
