package ru.kotletkin.positiongen.calculation.places;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceCsvReaderTest {

    @Test
    void readsLatitudeAndLongitude() throws Exception {
        List<PlacePoint> places = PlaceCsvReader.read(text("""
                latitude,longitude
                59.94,30.34
                59.95,30.36
                """));

        assertEquals(2, places.size());
        assertEquals(59.94, places.getFirst().latitude());
        assertEquals(30.36, places.get(1).longitude());
    }

    @Test
    void readsHeaderlessRowsAsLatitudeAndLongitude() throws Exception {
        List<PlacePoint> places = PlaceCsvReader.read(text("""
                59.94,30.34
                60.01,30.40
                """));

        assertEquals(59.94, places.getFirst().latitude());
        assertEquals(30.40, places.get(1).longitude());
    }

    @Test
    void readsSemicolonFilesWithCommaDecimals() throws Exception {
        List<PlacePoint> places = PlaceCsvReader.read(text("""
                latitude;longitude
                59,94;30,34
                """));

        assertEquals(59.94, places.getFirst().latitude());
        assertEquals(30.34, places.getFirst().longitude());
    }

    @Test
    void rejectsAFileWithoutCoordinates() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PlaceCsvReader.read(text("name,note\none,two\n")));

        assertEquals("Places file needs columns latitude,longitude", error.getMessage());
    }

    private static ByteArrayInputStream text(final String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
