package de.t_animal.journeyapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import de.t_animal.journeyapp.containers.Checkpoint;
import de.t_animal.journeyapp.containers.Coordinate;
import de.t_animal.journeyapp.containers.Zone;

public class JourneyProperties {

	private final String TAG = "JourneyProperties";

	private static JourneyProperties singleton;

	private Context context;
	private Properties prop;

	private Date journeyDate;
	private String[] checkpointNames;
	private String[] safezoneNames;

	private Coordinate start;
	private Checkpoint[] checkpoints;
	private Zone[] safeZones;
	private Zone[] offLimitsZones;

	public static JourneyProperties getInstance(Context context) {
		if (singleton == null) {
			singleton = new JourneyProperties(context);
		}

		return singleton;
	}

	public static JourneyProperties getInstance(Fragment frag) {
		return getInstance(frag.getActivity());
	}

	public String getJourneyID() {
		return "net.hawo.journey";
	}

	public JourneyProperties(Context context) {
		prop = new java.util.Properties();
		this.context = context;

		try {
			prop.loadFromXML(new FileInputStream(Environment.getExternalStorageDirectory().getPath()
					+ "/de.t_animal/journeyApp/" + getJourneyID() + "/properties.xml"));
		} catch (InvalidPropertiesFormatException e) {
			Toast.makeText(context, "Could not load properties", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Could not load properties", e);
			System.exit(0);
		} catch (IOException e) {
			Toast.makeText(context, "Could not load properties", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Could not load properties", e);
			System.exit(0);
		}

		try {
			journeyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(prop.getProperty("journeyDate"));
		} catch (ParseException e) {
			Log.e(this.getClass().getName(), "Could parse time", e);
			journeyDate = new Date(9999, 12, 31);
		}

		int checkpoints = Integer.parseInt(prop.getProperty("checkpointsCount", "0"));
		checkpointNames = new String[checkpoints];

		// get checkpoint names
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

		// try to get a safezone for each checkpoint
		safezoneNames = new String[checkpointNames.length];
		for (int i = 0; i < checkpointNames.length; i++) {
			safezoneNames[i] = prop.getProperty("safezone" + (i + 1));
			if (safezoneNames[i] == null)
				Toast.makeText(context, "Warning: No safezone for checkpoint " + checkpointNames[i], Toast.LENGTH_LONG)
						.show();
		}

		parseKMLFile();
	}

	private void initEmpty() {
		start = new Coordinate(0, 0);
		checkpoints = new Checkpoint[0];
		safeZones = new Zone[0];
		offLimitsZones = new Zone[0];
	}

	private void parseKMLFile() {
		try {
			// Parse XML file
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			JourneyKmlHandler myXMLHandler = new JourneyKmlHandler();

			xmlReader.setContentHandler(myXMLHandler);
			xmlReader.parse(new InputSource(new FileInputStream(Environment.getExternalStorageDirectory().getPath()
					+ "/de.t_animal/journeyApp/" + getJourneyID() + "/places.kml")));

			ArrayList<KMLPlacemark> placemarks = ((JourneyKmlHandler) xmlReader.getContentHandler()).placemarks;

			// Convert strings to coordinates
			ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
			ArrayList<Zone> safeZones = new ArrayList<Zone>();
			ArrayList<Zone> offLimitsZones = new ArrayList<Zone>();

			List<String> checkpointNames = Arrays.asList(this.checkpointNames);
			List<String> safezoneNames = Arrays.asList(this.safezoneNames);

			for (KMLPlacemark placemark : placemarks) {

				if (placemark.name.equalsIgnoreCase("start")) {
					String[] coords = placemark.coordinates.split(",");
					start = new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
				} else

				if (checkpointNames.contains(placemark.name)) {

					String[] coords = placemark.coordinates.split(",");
					Checkpoint newPoint = new Checkpoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]),
							placemark.name, placemark.description);

					// try to find the corresponding safezone (if existing and already in list)
					int cpIndex = checkpointNames.indexOf(placemark.name);
					for (Zone safeZone : safeZones) {
						if (safeZone.name.equals(safezoneNames.get(cpIndex))) {
							newPoint.safeZone = safeZone;
						}
					}

					checkpoints.add(newPoint);

				} else

				if (placemark.name.equalsIgnoreCase("safezone") || safezoneNames.contains(placemark.name)) {

					String[] coordlines = placemark.coordinates.split("\\s+");
					ArrayList<Coordinate> border = new ArrayList<Coordinate>();

					// get coordinates from comma seperated string
					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						border.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					Zone newZone = new Zone(placemark.name, placemark.description, border.toArray(new Coordinate[0]));

					// try to find the corresponding safezone (if existing and already in list)
					int szIndex = safezoneNames.indexOf(placemark.name);
					for (Checkpoint checkpoint : checkpoints) {
						if (checkpoint.name.equals(checkpointNames.get(szIndex))) {
							checkpoint.safeZone = newZone;
						}
					}

					safeZones.add(newZone);
				} else

				if (placemark.name.equalsIgnoreCase("offlimits")) {

					String[] coordlines = placemark.coordinates.split("\n");
					ArrayList<Coordinate> border = new ArrayList<Coordinate>();

					// get coordinates from comma seperated string
					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						border.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					offLimitsZones.add(new Zone(placemark.name, placemark.description, border
							.toArray(new Coordinate[0])));
				}

				this.checkpoints = checkpoints.toArray(new Checkpoint[0]);
				this.safeZones = safeZones.toArray(new Zone[0]);
				this.offLimitsZones = offLimitsZones.toArray(new Zone[0]);
			}

		} catch (Exception e) {
			initEmpty();
			e.printStackTrace();
		}

		if (checkpointNames.length != checkpoints.length)
			Log.w(TAG, "Length of checkpoints and checkpointsName do not match!");
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

	public String getServerLocation() {
		return prop.getProperty("serverLocation");
	}

	public int getServerPort() {
		return Integer.parseInt(prop.getProperty("serverPort"));
	}

	public Coordinate getStart() {
		return start;
	}

	public Checkpoint[] getCheckpoints() {
		return checkpoints;
	}

	public Zone[] getSafeZones() {
		return safeZones;
	}

	public Zone[] getOffLimitsZones() {
		return offLimitsZones;
	}

	public File getLocationFile() {
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ "/de.t_animal/journeyApp/"
				+ getJourneyID()
				+ "/locationData");
	}

	public File getUploadLocationFile() {
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ "/de.t_animal/journeyApp/"
				+ getJourneyID()
				+ "/locationDataUploadable");
	}

	/*
	 * Some helper and storage classes
	 */

	class KMLPlacemark {
		String name;
		String description;
		String coordinates;
	}

	private class JourneyKmlHandler extends DefaultHandler {

		private ArrayList<KMLPlacemark> placemarks = new ArrayList<KMLPlacemark>();
		private KMLPlacemark curElem = null;
		private String curValue;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equalsIgnoreCase("placemark"))
				curElem = new KMLPlacemark();
			curValue = "";
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (curElem == null)
				return;

			if (localName.equalsIgnoreCase("placemark"))
				placemarks.add(curElem);

			if (localName.equalsIgnoreCase("name")) {
				curElem.name = curValue;
			}

			if (localName.equalsIgnoreCase("description")) {
				curElem.description = curValue;
			}

			if (localName.equalsIgnoreCase("coordinates")) {
				curElem.coordinates = curValue;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (curElem != null)
				curValue = curValue + new String(ch, start, length);
		}
	}
}
