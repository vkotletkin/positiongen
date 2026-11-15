package ru.kotletkin.positiongen.map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.positiongen.config.MapProperties;
import ru.kotletkin.positiongen.exception.StorageException;
import ru.kotletkin.positiongen.result.ResultArchiveService;
import ru.kotletkin.positiongen.result.StoredArtifacts;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MapService {

    private final ResultArchiveService results;
    private final MapProperties properties;

    public GeoJsonResponse geoJson(final UUID calculationId) {
        StoredArtifacts files = results.getArtifactsInfo(calculationId);
        try (InputStream input = results.openFileByObjectKey(files.xyObjectKey())) {
            return TrackGeoJson.fromCsv(
                    input,
                    files.reportCount(),
                    properties.originLatitude(),
                    properties.originLongitude(),
                    properties.maxPoints());
        } catch (IOException ex) {
            throw new StorageException("Failed to parse XY.csv", ex);
        }
    }
}
