package ru.kotletkin.totleben.map;

import java.util.UUID;

public interface MapService {

    GeoJsonResponse geoJson(UUID calculationId);
}
