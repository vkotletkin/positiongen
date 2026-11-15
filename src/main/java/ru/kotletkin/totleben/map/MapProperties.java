package ru.kotletkin.totleben.map;

import org.springframework.boot.context.properties.ConfigurationProperties;

// TODO: вынеси в config общий где сейчас и storage configuration
@ConfigurationProperties(prefix = "totleben.map")
public record MapProperties(
        double originLatitude,
        double originLongitude,
        int maxPoints
) {
}
