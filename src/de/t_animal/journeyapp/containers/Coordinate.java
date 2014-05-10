package de.t_animal.journeyapp.containers;

public class Coordinate {
	public double lat;
	public double lon;

	public Coordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "{\"lat\": " + lat + ", \"lon\": " + lon + "}";
	}
}
