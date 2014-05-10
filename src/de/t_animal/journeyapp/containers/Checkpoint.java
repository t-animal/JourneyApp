package de.t_animal.journeyapp.containers;


public class Checkpoint extends Coordinate {
	public String name;
	public String description;
	public Zone safeZone;

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
