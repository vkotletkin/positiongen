package ru.kotletkin.positiongen.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.calculation")
public record CalculationProperties(
        @DefaultValue("2") @Min(1) @Max(32) int poolSize
) {
}
