package ru.kotletkin.totleben.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.kotletkin.totleben.exception.StorageException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Path;

@Component
@EnableConfigurationProperties(StorageProperties.class)
public class SeaweedObjectStorage implements ObjectStorage {

    private final S3Client client;
    private final StorageProperties properties;
    private final String endpoint;
    private boolean bucketReady;

    public SeaweedObjectStorage(
            final S3Client client,
            final StorageProperties properties,
            @Value("${spring.cloud.aws.s3.endpoint}") final String endpoint) {
        this.client = client;
        this.properties = properties;
        this.endpoint = endpoint;
    }

    @Override
    public void put(final String key, final Path file, final String contentType) {
        ensureBucket();
        try {
            client.putObject(PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build(), file);
        } catch (SdkException ex) {
            throw storageFailure("Не удалось записать объект в SeaweedFS", ex);
        }
    }

    @Override
    public InputStream open(final String key) {
        try {
            return client.getObject(GetObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
        } catch (SdkException ex) {
            throw storageFailure("Не удалось прочитать объект из SeaweedFS", ex);
        }
    }

    private void ensureBucket() {
        if (bucketReady) {
            return;
        }
        synchronized (this) {
            if (bucketReady) {
                return;
            }
            try {
                client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build());
            } catch (NoSuchBucketException ex) {
                createBucket();
            } catch (S3Exception ex) {
                if (ex.statusCode() == 404) {
                    createBucket();
                } else {
                    throw storageFailure("Не удалось открыть бакет SeaweedFS", ex);
                }
            } catch (SdkClientException ex) {
                throw storageFailure("SeaweedFS недоступен по адресу " + endpoint, ex);
            }
            bucketReady = true;
        }
    }

    private void createBucket() {
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket()).build());
        } catch (S3Exception ex) {
            if (ex.statusCode() != 409) {
                throw storageFailure("Не удалось создать бакет SeaweedFS", ex);
            }
        } catch (SdkClientException ex) {
            throw storageFailure("SeaweedFS недоступен по адресу " + endpoint, ex);
        }
    }

    private static StorageException storageFailure(final String message, final SdkException ex) {
        String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return new StorageException(message + ": " + detail, ex);
    }
}
