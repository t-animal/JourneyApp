package de.t_animal.journeyapp.containers;

import java.util.Arrays;

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
			if (vectorsIntersect(border[i], border[(i + 1) % border.length],
					new Coordinate(location.getLatitude(), location.getLongitude())))
				intersectingEdges++;
		}
		// if an odd number of intersections, the point is inside the polygon
		return intersectingEdges % 2 == 1;
	}

	private boolean vectorsIntersect(Coordinate a1, Coordinate a2, Coordinate b) {
		Coordinate origin = new Coordinate(0, 0);
		return (counterClockWise(a1, a2, b) * counterClockWise(a1, a2, origin) <= 0)
				&& (counterClockWise(b, origin, a1) * counterClockWise(b, origin, a2) <= 0);
	}

	private int counterClockWise(Coordinate a1, Coordinate a2, Coordinate b) {
		double ax = a2.lat - a1.lat;
		double ay = a2.lon - a2.lon;
		double bx = b.lat - a1.lat;
		double by = b.lon - a1.lon;

		double ccw = bx * ay - by * ax;

		if (ccw == 0.) {
			ccw = bx * ax + by * ay;

			if (ccw > 0.) {
				bx -= ax;
				by -= ay;
				ccw = bx * ax + by * ay;

				if (ccw < 0.) {
					ccw = 0.;
				}
			}
		}

		if (ccw < 0.0)
			return -1;
		else if (ccw > 0.0)
			return 1;

		return 0;
	}
}
