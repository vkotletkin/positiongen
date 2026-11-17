package ru.kotletkin.positiongen.simulation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import ru.kotletkin.positiongen.map.GeoJsonResponse;
import ru.kotletkin.positiongen.map.TrackGeoJson;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractalRabbitCalculatorTest {

    @Test
    void writesBothResultFilesAndAMapTrack() throws Exception {
        FractalRabbitCalculator calculator = new FractalRabbitCalculator();
        SimulationRequest request = new SimulationRequest(
                2, 1.33, 40, 3, 1, 14, 8, 0.75, -2.0, 100.0, 20.0, 10.0, 100.0, 0.001, -1.5, false, 15, null);

        try (SimulationOutput output = calculator.run(request)) {
            assertTrue(output.reportCount() > 0, "expected reports within a 14-day window");
            assertEquals(1, output.coTravelerIds().size());
            assertTrue(output.coTravelerIds().getFirst() >= 0);
            assertTrue(output.coTravelerIds().getFirst() < request.travelerCount());
            assertEquals(output.reportCount(), dataRows(output.placesCsv()));
            try (InputStream input = Files.newInputStream(output.xyCsv())) {
                GeoJsonResponse geoJson = TrackGeoJson.fromCsv(input, output.reportCount(), 44.605, 33.522, 10_000);
                assertFalse(geoJson.truncated());
                assertEquals(output.reportCount(), geoJson.drawnPoints());
                assertTrue(geoJson.collection().features().stream()
                        .anyMatch(feature -> "LineString".equals(feature.geometry().type())));
            }
        }
    }

    @Test
    void usesUploadedPlacesForEveryReport() throws Exception {
        double kilometersPerUnit = 20.0;
        double[][] places = {
                {0, 0},
                {1, 0},
                {0, 1},
                {1, 1}
        };
        SimulationRequest request = new SimulationRequest(
                2, 5, 9_999, 2, 0, 14, 6, 0.75, -2.0, 100.0, kilometersPerUnit, 4.0, 30.0, 0.001, -1.5, false, 15, places);

        try (SimulationOutput output = new FractalRabbitCalculator().run(request)) {
            assertTrue(output.reportCount() > 0);
            try (CSVParser parser = CSVParser.parse(output.xyCsv(), java.nio.charset.StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
                assertEquals(output.reportCount(), parser.getRecords().size());
            }
            try (CSVParser parser = CSVParser.parse(output.xyCsv(), java.nio.charset.StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
                for (CSVRecord row : parser) {
                    double x = Double.parseDouble(row.get("x(km)"));
                    double y = Double.parseDouble(row.get("y(km)"));
                    assertTrue(isUploadedCorner(x, y, kilometersPerUnit), x + "," + y);
                }
            }
            try (CSVParser parser = CSVParser.parse(output.placesCsv(), java.nio.charset.StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
                for (CSVRecord row : parser) {
                    int place = Integer.parseInt(row.get("Place ID"));
                    assertTrue(place >= 0 && place < places.length);
                }
            }
        }
    }

    @Test
    void companionsVisitTheSamePlacesWithinTheTimeWindow() throws Exception {
        double[][] places = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        SimulationRequest request = new SimulationRequest(
                2, 1.33, 4, 2, 2, 14, 12, 0.75, -2.0, 100.0, 20.0, 4.0, 20.0, 0.001, -1.5, true, 15, places);
        double windowDays = 15.0 / 1440.0;

        try (SimulationOutput output = new FractalRabbitCalculator().run(request)) {
            assertTrue(output.reportCount() > 0);
            Map<Integer, List<CSVRecord>> byTraveler = reportsByTraveler(output.placesCsv());
            List<Integer> companions = output.coTravelerIds();
            assertEquals(2, companions.size());
            List<CSVRecord> first = byTraveler.get(companions.get(0));
            List<CSVRecord> second = byTraveler.get(companions.get(1));
            assertEquals(first.size(), second.size());
            assertFalse(first.isEmpty());
            for (int index = 0; index < first.size(); index++) {
                assertEquals(first.get(index).get("Place ID"), second.get(index).get("Place ID"));
                double left = Double.parseDouble(first.get(index).get("Days"));
                double right = Double.parseDouble(second.get(index).get("Days"));
                assertTrue(Math.abs(left - right) <= windowDays + 1e-9, left + " vs " + right);
            }
        }
    }

    @Test
    void projectionKeepsTheAnchorAndMovesNorth() {
        double[] origin = TrackGeoJson.project(0, 0, 44.605, 33.522);
        assertEquals(33.522, origin[0], 1e-9);
        assertEquals(44.605, origin[1], 1e-9);

        double[] north = TrackGeoJson.project(0, 110.574, 44.605, 33.522);
        assertEquals(33.522, north[0], 1e-6);
        assertEquals(45.605, north[1], 1e-9);
    }

    private static boolean isUploadedCorner(final double xKm, final double yKm, final double kilometersPerUnit) {
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                if (Math.abs(xKm - x * kilometersPerUnit) < 1e-9 && Math.abs(yKm - y * kilometersPerUnit) < 1e-9) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<Integer, List<CSVRecord>> reportsByTraveler(java.nio.file.Path csv) throws Exception {
        Map<Integer, List<CSVRecord>> grouped = new LinkedHashMap<>();
        try (CSVParser parser = CSVParser.parse(csv, java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
            for (CSVRecord row : parser) {
                grouped.computeIfAbsent(Integer.parseInt(row.get("Traveler ID")), _ -> new ArrayList<>()).add(row);
            }
        }
        return grouped;
    }

    private static int dataRows(java.nio.file.Path csv) throws Exception {
        try (CSVParser parser = CSVParser.parse(csv, java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
            return parser.getRecords().size();
        }
    }
}
