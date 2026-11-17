package ru.kotletkin.positiongen.calculation.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One uploaded place. Longitude is x, latitude is y.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlacePoint(Double latitude, Double longitude) {
}
