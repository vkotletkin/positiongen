package ru.kotletkin.positiongen.calculation.places;

import org.junit.jupiter.api.Test;
import ru.kotletkin.positiongen.map.TrackGeoJson;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceCoordinatesTest {

    private static final double ORIGIN_LATITUDE = 59.9343;
    private static final double ORIGIN_LONGITUDE = 30.3351;
    private static final double KILOMETERS_PER_UNIT = 20.0;

    @Test
    void geographicPlacesRoundTripThroughTheMapProjection() {
        double latitude = ORIGIN_LATITUDE + 1;
        double longitude = ORIGIN_LONGITUDE;
        double[][] model = PlaceCoordinates.toModelUnits(
                List.of(new PlacePoint(latitude, longitude)),
                ORIGIN_LATITUDE,
                ORIGIN_LONGITUDE,
                KILOMETERS_PER_UNIT);

        double xKm = model[0][0] * KILOMETERS_PER_UNIT;
        double yKm = model[0][1] * KILOMETERS_PER_UNIT;
        double[] projected = TrackGeoJson.project(xKm, yKm, ORIGIN_LATITUDE, ORIGIN_LONGITUDE);
        assertEquals(longitude, projected[0], 1e-9);
        assertEquals(latitude, projected[1], 1e-9);
        assertEquals(110.574, yKm, 1e-9);
    }

    @Test
    void rejectsARepeatedPlace() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> PlaceCoordinates.toModelUnits(
                List.of(new PlacePoint(59.94, 30.34), new PlacePoint(59.94, 30.34)),
                ORIGIN_LATITUDE,
                ORIGIN_LONGITUDE,
                KILOMETERS_PER_UNIT));

        assertEquals("Place 2 repeats an earlier place", error.getMessage());
    }

    @Test
    void rejectsALatitudeOutsideTheGlobe() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> PlaceCoordinates.toModelUnits(
                List.of(new PlacePoint(120.0, 30.0)),
                ORIGIN_LATITUDE,
                ORIGIN_LONGITUDE,
                KILOMETERS_PER_UNIT));

        assertEquals("Place 1: latitude must be from -90 to 90", error.getMessage());
    }
}
