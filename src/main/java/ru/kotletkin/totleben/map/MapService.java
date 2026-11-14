package ru.kotletkin.totleben.map;

import ru.kotletkin.totleben.map.dto.GeoJsonResponse;

import java.util.UUID;

public interface MapService {

    GeoJsonResponse geoJson(UUID calculationId);
}
