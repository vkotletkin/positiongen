package ru.kotletkin.positiongen.map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/calculations/{id}/geojson")
@RequiredArgsConstructor
@Tag(name = "Map", description = "Отрисовка результатов на карте")
public class MapController {

    private final MapService mapService;

    @GetMapping
    @Operation(summary = "Calculation track as GeoJSON")
    public GeoJsonResponse getGeoJsonById(@PathVariable final UUID id) {
        return mapService.geoJson(id);
    }
}
