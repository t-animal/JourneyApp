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
	private Coordinate[][] safeZones;
	private Coordinate[][] offLimitsZones;

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
		safeZones = new Coordinate[0][0];
		offLimitsZones = new Coordinate[0][0];
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
			ArrayList<ArrayList<Coordinate>> safeZones = new ArrayList<ArrayList<Coordinate>>();
			ArrayList<ArrayList<Coordinate>> offLimitsZones = new ArrayList<ArrayList<Coordinate>>();

			List<String> checkpointNames = Arrays.asList(this.checkpointNames);
			for (KMLPlacemark placemark : placemarks) {

				if (placemark.name.equalsIgnoreCase("start")) {
					String[] coords = placemark.coordinates.split(",");
					start = new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
				} else

				if (checkpointNames.contains(placemark.name)) {

					String[] coords = placemark.coordinates.split(",");
					checkpoints.add(new Checkpoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]),
							placemark.name));

				} else

				if (placemark.name.equalsIgnoreCase("safezone")) {

					String[] coordlines = placemark.coordinates.split("\n");
					ArrayList<Coordinate> safeZone = new ArrayList<Coordinate>();

					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						safeZone.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					safeZones.add(safeZone);
				} else

				if (placemark.name.equalsIgnoreCase("offlimits")) {

					String[] coordlines = placemark.coordinates.split("\n");
					ArrayList<Coordinate> offlimitsZone = new ArrayList<Coordinate>();

					for (String coordline : coordlines) {
						String[] coords = coordline.split(",");
						if (coords.length != 3)
							continue;
						offlimitsZone.add(new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
					}

					offLimitsZones.add(offlimitsZone);
				}

				this.checkpoints = checkpoints.toArray(new Checkpoint[0]);

				this.safeZones = new Coordinate[safeZones.size()][];
				for (int i = 0; i < this.safeZones.length; i++) {
					this.safeZones[i] = safeZones.get(i).toArray(new Coordinate[0]);
				}

				this.offLimitsZones = new Coordinate[offLimitsZones.size()][];
				for (int i = 0; i < this.offLimitsZones.length; i++) {
					this.offLimitsZones[i] = offLimitsZones.get(i).toArray(new Coordinate[0]);
				}
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

	public Coordinate getStart() {
		return start;
	}

	public Checkpoint[] getCheckpoints() {
		return checkpoints;
	}

	public Coordinate[][] getSafeZones() {
		return safeZones;
	}

	public Coordinate[][] getOffLimitsZones() {
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
		String description = "Description goes here";

		public Checkpoint(double lat, double lon, String name) {
			super(lat, lon);
			this.name = name;
		}

		@Override
		public String toString() {
			return "{\"lat\": " + lat + ", \"lon\": " + lon + ", \"no\": \"" + name + "\"}";
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
