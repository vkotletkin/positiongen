package ru.kotletkin.totleben.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "totleben.storage")
public record StorageProperties(String bucket) {
}
