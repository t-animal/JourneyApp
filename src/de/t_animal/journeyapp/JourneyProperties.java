package de.t_animal.journeyapp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class JourneyProperties {

	private final String TAG = "JourneyProperties";

	private static JourneyProperties singleton;

	private Context context;
	private Properties prop;

	private Date journeyDate;
	private String[] checkpointNames;

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

	public JourneyProperties(Context context) {
		prop = new java.util.Properties();
		this.context = context;

		try {
			prop.loadFromXML(context.getAssets().open("properties.xml"));
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
			xmlReader.parse(new InputSource(context.getAssets().open("places.kml")));

			ArrayList<KMLPlacemark> placemarks = ((JourneyKmlHandler) xmlReader.getContentHandler()).placemarks;

			// Convert strings to coordinates
			ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
			ArrayList<Zone> safeZones = new ArrayList<Zone>();
			ArrayList<Zone> offLimitsZones = new ArrayList<Zone>();

			List<String> checkpointNames = Arrays.asList(this.checkpointNames);
			for (KMLPlacemark placemark : placemarks) {

				if (placemark.name.equalsIgnoreCase("start")) {
					String[] coords = placemark.coordinates.split(",");
					start = new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
				} else

				if (checkpointNames.contains(placemark.name)) {

					String[] coords = placemark.coordinates.split(",");
					checkpoints.add(new Checkpoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]),
							placemark.name, placemark.description));

				} else

				if (placemark.name.equalsIgnoreCase("safezone")) {

					String[] coordlines = placemark.coordinates.split("\n");
					ArrayList<Coordinate> border = new ArrayList<Coordinate>();

					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						border.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					safeZones.add(new Zone(placemark.description, border.toArray(new Coordinate[0])));
				} else

				if (placemark.name.equalsIgnoreCase("offlimits")) {

					String[] coordlines = placemark.coordinates.split("\n");
					ArrayList<Coordinate> border = new ArrayList<Coordinate>();

					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						border.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					offLimitsZones.add(new Zone(placemark.description, border.toArray(new Coordinate[0])));
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

	/*
	 * Some helper and storage classes
	 */

	class KMLPlacemark {
		String name;
		String description;
		String coordinates;
	}

	class Coordinate {
		double lat;
		double lon;

		public Coordinate(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public String toString() {
			return "{\"lat\": " + lat + ", \"lon\": " + lon + "}";
		}
	}

	class Checkpoint extends Coordinate {
		String name;
		String description;

		public Checkpoint(double lat, double lon, String name, String description) {
			super(lat, lon);
			this.name = name;
			this.description = description.replaceAll("<br>", "\n").replaceAll("<.*?>", "");
		}

		@Override
		public String toString() {
			return "{\"lat\": " + lat + ", \"lon\": " + lon + ", \"no\": \"" + name + "\"}";
		}
	}

	class Zone {
		String description;
		Coordinate[] border;

		public Zone(String description, Coordinate[] border) {
			this.description = description.replaceAll("<br>", "\n").replaceAll("<.*?>", "");
			this.border = border;
		}

		@Override
		public String toString() {
			return Arrays.deepToString(border);
		}
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
