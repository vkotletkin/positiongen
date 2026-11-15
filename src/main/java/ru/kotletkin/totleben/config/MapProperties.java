package ru.kotletkin.totleben.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.map")
public record MapProperties(
        double originLatitude,
        double originLongitude,
        int maxPoints
) {
}
