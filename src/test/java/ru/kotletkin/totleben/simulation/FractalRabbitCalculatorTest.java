package ru.kotletkin.totleben.simulation;

import org.apache.commons.csv.CSVFormat;
import ru.kotletkin.totleben.map.TrackGeoJson;
import ru.kotletkin.totleben.map.GeoJsonResponse;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractalRabbitCalculatorTest {

    @Test
    void writesBothResultFilesAndAMapTrack() throws Exception {
        FractalRabbitCalculator calculator = new FractalRabbitCalculator();
        SimulationRequest request = new SimulationRequest(2, 1.33, 40, 3, 1, 14, 8);

        try (SimulationOutput output = calculator.run(request)) {
            assertTrue(output.reportCount() > 0, "ожидались отчёты в окне 14 суток");
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
    void projectionKeepsTheAnchorAndMovesNorth() {
        double[] origin = TrackGeoJson.project(0, 0, 44.605, 33.522);
        assertEquals(33.522, origin[0], 1e-9);
        assertEquals(44.605, origin[1], 1e-9);

        double[] north = TrackGeoJson.project(0, 110.574, 44.605, 33.522);
        assertEquals(33.522, north[0], 1e-6);
        assertEquals(45.605, north[1], 1e-9);
    }

    private static int dataRows(java.nio.file.Path csv) throws Exception {
        try (CSVParser parser = CSVParser.parse(csv, java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
            return parser.getRecords().size();
        }
    }
}
