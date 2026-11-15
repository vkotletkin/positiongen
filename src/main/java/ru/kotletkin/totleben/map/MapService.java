package ru.kotletkin.totleben.map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.config.MapProperties;
import ru.kotletkin.totleben.exception.StorageException;
import ru.kotletkin.totleben.result.ResultArchive;
import ru.kotletkin.totleben.result.StoredArtifacts;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MapService {

    private final ResultArchive results;
    private final MapProperties properties;

    public GeoJsonResponse geoJson(final UUID calculationId) {
        StoredArtifacts files = results.files(calculationId);
        try (InputStream input = results.open(files.xyObjectKey())) {
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
