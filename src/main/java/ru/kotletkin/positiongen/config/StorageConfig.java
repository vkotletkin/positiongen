package ru.kotletkin.positiongen.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.storage")
public record StorageConfig(@NotBlank String bucket) {
}
