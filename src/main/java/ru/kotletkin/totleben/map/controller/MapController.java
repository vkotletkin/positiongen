package ru.kotletkin.totleben.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kotletkin.totleben.config.openapi.ApiStandardErrorResponses;
import ru.kotletkin.totleben.map.MapService;
import ru.kotletkin.totleben.map.dto.GeoJsonResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/calculations/{id}/geojson")
@RequiredArgsConstructor
@ApiStandardErrorResponses
@Tag(name = "Map", description = "Отрисовка результатов на карте")
public class MapController {

    private final MapService maps;

    @GetMapping
    @Operation(summary = "Calculation track as GeoJSON")
    public GeoJsonResponse geoJson(@PathVariable final UUID id) {
        return maps.geoJson(id);
    }
}
