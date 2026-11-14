package ru.kotletkin.totleben.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kotletkin.totleben.exception.StorageException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class SeaweedObjectStorage implements ObjectStorage {

    private final S3Client client;
    private final StorageProperties properties;
    private boolean bucketReady;

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
                throw storageFailure("SeaweedFS недоступен по адресу " + properties.endpoint(), ex);
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
            throw storageFailure("SeaweedFS недоступен по адресу " + properties.endpoint(), ex);
        }
    }

    private static StorageException storageFailure(final String message, final SdkException ex) {
        String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return new StorageException(message + ": " + detail, ex);
    }
}
