package ru.kotletkin.totleben.map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.config.MapProperties;
import ru.kotletkin.totleben.exception.StorageException;
import ru.kotletkin.totleben.result.ResultArchiveService;
import ru.kotletkin.totleben.result.StoredArtifacts;

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
            throw new StorageException("Не удалось разобрать XY.csv", ex);
        }
    }
}
