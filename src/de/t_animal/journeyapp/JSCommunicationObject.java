package de.t_animal.journeyapp;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * A class to be inserted into the JS and act as a communication bridge through it's getters and setters
 */
public class JSCommunicationObject {

	private static JSCommunicationObject singleton;

	public static JSCommunicationObject getInstance(Context context) {
		if (singleton == null) {
			singleton = new JSCommunicationObject(context);
		}
		return singleton;
	}

	private class KMLPlacemark {
		String name;
		String description;
		String coordinates;
	}

	private class Coordinate {
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

	private class Checkpoint extends Coordinate {
		int no;

		public Checkpoint(double lat, double lon, int no) {
			super(lat, lon);
			this.no = no;
		}

		@Override
		public String toString() {
			return "{\"lat\": " + lat + ", \"lon\": " + lon + ", \"no\": " + no + "}";
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

	private Context context;

	Coordinate start;
	Checkpoint[] checkpoints;
	Coordinate[][] safeZones;
	Coordinate[][] offLimitsZones;

	private JSCommunicationObject(Context context) {
		this.context = context;
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

			int checkpointCount = 1;
			for (KMLPlacemark placemark : placemarks) {

				if (placemark.name.equalsIgnoreCase("start")) {
					String[] coords = placemark.coordinates.split(",");
					start = new Coordinate(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
				} else

				if (placemark.name.equalsIgnoreCase("checkpoint")) {

					String[] coords = placemark.coordinates.split(",");
					checkpoints.add(new Checkpoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]),
							checkpointCount++));

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
	}

	@JavascriptInterface
	public String getTheme() {
		return Preferences.isCaught(context) ? "THEME_CHASER" : "THEME_RUNNER";
	}

	@JavascriptInterface
	public String getStart() {
		return start.toString();
	}

	@JavascriptInterface
	public String getCheckpoints() {
		return Arrays.deepToString(checkpoints);
	}

	@JavascriptInterface
	public String getSafeZones() {
		return Arrays.deepToString(safeZones);
	}

	@JavascriptInterface
	public String getOffLimitsZones() {
		return Arrays.deepToString(offLimitsZones);
	}

	@JavascriptInterface
	public boolean isFollowingUser() {
		return Preferences.mapFollowsUser(context);
	}

	@JavascriptInterface
	public void setFollowingUser(boolean followingUser) {
		Preferences.mapFollowsUser(context, followingUser);
	}
}
