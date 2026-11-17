package ru.kotletkin.positiongen.calculation.places;

import ru.kotletkin.positiongen.map.TrackGeoJson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks an uploaded place set and converts latitude and longitude into the
 * model coordinates the route generator expects. Longitude is x, latitude is y.
 */
public final class PlaceCoordinates {

    public static final int MAX_PLACES = 20_000;

    private PlaceCoordinates() {
    }

    public static double[][] toModelUnits(final List<PlacePoint> places,
                                          final double originLatitude,
                                          final double originLongitude,
                                          final double kilometersPerUnit) {
        if (places == null || places.isEmpty()) {
            throw new IllegalArgumentException("At least one place is required");
        }
        if (places.size() > MAX_PLACES) {
            throw new IllegalArgumentException("At most 20,000 places");
        }
        if (!Double.isFinite(kilometersPerUnit) || kilometersPerUnit <= 0) {
            throw new IllegalArgumentException("Kilometers per model unit must be greater than zero");
        }
        double[][] model = new double[places.size()][];
        Set<CoordinateKey> seen = new HashSet<>();
        for (int index = 0; index < places.size(); index++) {
            PlacePoint place = places.get(index);
            int number = index + 1;
            if (place == null) {
                throw new IllegalArgumentException("Place " + number + " is empty");
            }
            if (place.latitude() == null || place.longitude() == null) {
                throw new IllegalArgumentException("Place " + number + ": latitude and longitude are required");
            }
            double latitude = finite(place.latitude(), number, "latitude");
            double longitude = finite(place.longitude(), number, "longitude");
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Place " + number + ": latitude must be from -90 to 90");
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Place " + number + ": longitude must be from -180 to 180");
            }
            double[] kilometers = TrackGeoJson.kilometersFromGeographic(
                    latitude, longitude, originLatitude, originLongitude);
            double[] coordinates = new double[]{kilometers[0] / kilometersPerUnit, kilometers[1] / kilometersPerUnit};
            if (!Double.isFinite(coordinates[0]) || !Double.isFinite(coordinates[1])) {
                throw new IllegalArgumentException("Place " + number + " is too far from the map origin");
            }
            if (!seen.add(new CoordinateKey(coordinates[0], coordinates[1]))) {
                throw new IllegalArgumentException("Place " + number + " repeats an earlier place");
            }
            model[index] = coordinates;
        }
        return model;
    }

    private static double finite(final Double value, final int number, final String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Place " + number + ": " + name + " must be a finite number");
        }
        return value;
    }

    private record CoordinateKey(long xBits, long yBits) {
        private CoordinateKey(final double x, final double y) {
            this(Double.doubleToLongBits(x == 0.0 ? 0.0 : x), Double.doubleToLongBits(y == 0.0 ? 0.0 : y));
        }
    }
}
