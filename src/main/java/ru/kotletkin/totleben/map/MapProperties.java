package ru.kotletkin.totleben.map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "totleben.map")
public record MapProperties(
        double originLatitude,
        double originLongitude,
        int maxPoints
) {
}
