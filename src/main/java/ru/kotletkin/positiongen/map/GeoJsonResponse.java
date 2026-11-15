package ru.kotletkin.positiongen.map;

import java.util.List;
import java.util.Map;

public record GeoJsonResponse(
        boolean truncated,
        int reportCount,
        int drawnPoints,
        double originLatitude,
        double originLongitude,
        FeatureCollection collection
) {

    public record FeatureCollection(String type, List<Feature> features) {
    }

    public record Feature(String type, Geometry geometry, Map<String, Object> properties) {
    }

    public record Geometry(String type, Object coordinates) {
    }
}
