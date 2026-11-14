package ru.kotletkin.totleben.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kotletkin.totleben.config.StorageConfig;
import ru.kotletkin.totleben.exception.StorageException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class ObjectStorageService {

    private final S3Client client;
    private final StorageConfig storageConfig;

    @PostConstruct
    public void init() {
        try {
            client.headBucket(HeadBucketRequest.builder()
                    .bucket(storageConfig.bucket())
                    .build());
        } catch (NoSuchBucketException _) {
            createBucket();
        } catch (SdkException ex) {
            throw new StorageException("Failed to access object storage", ex);
        }
    }

    public void put(final String key, final Path file, final String contentType) {
        try {
            client.putObject(PutObjectRequest.builder()
                    .bucket(storageConfig.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build(), file);
        } catch (SdkException ex) {
            throw new StorageException("Failed to write object", ex);
        }
    }

    public InputStream open(final String key) {
        try {
            return client.getObject(GetObjectRequest.builder()
                    .bucket(storageConfig.bucket())
                    .key(key)
                    .build());
        } catch (SdkException ex) {
            throw new StorageException("Failed to read object", ex);
        }
    }

    private void createBucket() {
        try {
            client.createBucket(CreateBucketRequest.builder()
                    .bucket(storageConfig.bucket())
                    .build());
        } catch (S3Exception ex) {
            // 409 Conflict означает, что бакет уже был создан другим инстансом приложения при параллельном старте
            if (ex.statusCode() != 409) {
                throw new StorageException("Cannot create bucket in object storage", ex);
            }
        } catch (SdkException ex) {
            throw new StorageException("Failed to access object storage", ex);
        }
    }
}
