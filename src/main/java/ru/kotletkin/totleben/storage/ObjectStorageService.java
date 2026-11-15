package ru.kotletkin.totleben.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.config.StorageConfig;
import ru.kotletkin.totleben.exception.StorageException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageConfig storageConfig;

    @PostConstruct
    public void init() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
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
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(storageConfig.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build(), file);
        } catch (SdkException ex) {
            throw new StorageException("Failed to write object", ex);
        }
    }

    public URL presignGet(final String key, final Duration ttl) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(storageConfig.bucket())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url();
        } catch (SdkException ex) {
            throw new StorageException("Failed to generate presigned GET url", ex);
        }
    }

    public InputStream open(final String key) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(storageConfig.bucket())
                    .key(key)
                    .build());
        } catch (SdkException ex) {
            throw new StorageException("Failed to read object", ex);
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
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
