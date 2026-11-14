package ru.kotletkin.totleben.storage;

import io.awspring.cloud.autoconfigure.s3.S3ClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;

@Configuration
public class SeaweedClientConfiguration {

    @Bean
    S3ClientCustomizer seaweedChecksums() {
        return builder -> {
            builder.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED);
            builder.responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);
        };
    }
}
