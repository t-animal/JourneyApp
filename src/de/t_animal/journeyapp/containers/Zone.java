package de.t_animal.journeyapp.containers;

import java.util.Arrays;

import de.t_animal.journeyapp.util.Line2D;

import android.location.Location;

public class Zone {
	public String name;
	public String description;
	public Coordinate[] border;

	public Zone(String name, String description, Coordinate[] border) {
		this.name = name;
		this.description = description.replaceAll("<br>", "\n").replaceAll("<.*?>", "");
		this.border = border;
	}

	@Override
	public String toString() {
		return Arrays.deepToString(border);
	}

	public boolean containsLocation(Location location) {
		int intersectingEdges = 0;
		for (int i = 0; i < border.length; i++) {
			if (Line2D.linesIntersect(0, 0,
					location.getLatitude(), location.getLongitude(),
					border[i].lat, border[i].lon,
					border[(i + 1) % border.length].lat, border[(i + 1) % border.length].lon))

				intersectingEdges++;
		}
		// if an odd number of intersections, the point is inside the polygon
		return intersectingEdges % 2 == 1;
	}
}
