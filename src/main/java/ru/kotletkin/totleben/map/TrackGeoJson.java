package ru.kotletkin.totleben.map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class TrackGeoJson {

    private static final double KILOMETERS_PER_LATITUDE_DEGREE = 110.574;
    private static final double KILOMETERS_PER_LONGITUDE_DEGREE = 111.320;

    private TrackGeoJson() {
    }

    public static GeoJsonResponse fromCsv(InputStream csv,
                                          int storedReportCount,
                                          double originLatitude,
                                          double originLongitude,
                                          int maxPoints) throws IOException {
        Map<Integer, List<Report>> grouped = new LinkedHashMap<>();
        int kept = 0;
        boolean truncated = false;
        try (Reader reader = new InputStreamReader(csv, StandardCharsets.UTF_8)) {
            // TODO: withFirstRecordAsHeader()' is deprecated
            for (CSVRecord row : CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
                if (kept >= maxPoints) {
                    truncated = true;
                    break;
                }
                int travelerId = Integer.parseInt(row.get("ID"));
                double days = Double.parseDouble(row.get("Days"));
                double xKm = Double.parseDouble(row.get("x(km)"));
                double yKm = Double.parseDouble(row.get("y(km)"));
                if (Double.isFinite(days) && Double.isFinite(xKm) && Double.isFinite(yKm)) {
                    grouped.computeIfAbsent(travelerId, _ -> new ArrayList<>())
                            .add(new Report(travelerId, days, xKm, yKm));
                    kept++;
                }
            }
        }

        List<GeoJsonResponse.Feature> features = new ArrayList<>();
        int drawnPoints = 0;
        for (List<Report> reports : grouped.values()) {
            reports.sort(Comparator.comparingDouble(Report::days));
            List<List<Double>> line = new ArrayList<>();
            for (Report report : reports) {
                double[] lonLat = project(report.xKm(), report.yKm(), originLatitude, originLongitude);
                Map<String, Object> pointProperties = new LinkedHashMap<>();
                pointProperties.put("travelerId", report.travelerId());
                pointProperties.put("days", report.days());
                pointProperties.put("xKm", report.xKm());
                pointProperties.put("yKm", report.yKm());
                features.add(new GeoJsonResponse.Feature(
                        "Feature",
                        new GeoJsonResponse.Geometry("Point", List.of(lonLat[0], lonLat[1])),
                        pointProperties));
                line.add(List.of(lonLat[0], lonLat[1]));
                drawnPoints++;
            }
            if (line.size() >= 2) {
                Report first = reports.getFirst();
                Map<String, Object> lineProperties = new LinkedHashMap<>();
                lineProperties.put("travelerId", first.travelerId());
                lineProperties.put("kind", "track");
                lineProperties.put("reportCount", reports.size());
                features.add(new GeoJsonResponse.Feature(
                        "Feature",
                        new GeoJsonResponse.Geometry("LineString", line),
                        lineProperties));
            }
        }
        return new GeoJsonResponse(
                truncated,
                storedReportCount,
                drawnPoints,
                originLatitude,
                originLongitude,
                new GeoJsonResponse.FeatureCollection("FeatureCollection", features));
    }

    public static double[] project(double xKm, double yKm, double originLatitude, double originLongitude) {
        double latitude = originLatitude + yKm / KILOMETERS_PER_LATITUDE_DEGREE;
        double longitude = originLongitude + xKm / (KILOMETERS_PER_LONGITUDE_DEGREE * Math.cos(Math.toRadians(originLatitude)));
        return new double[]{longitude, latitude};
    }

    private record Report(int travelerId, double days, double xKm, double yKm) {
    }
}
