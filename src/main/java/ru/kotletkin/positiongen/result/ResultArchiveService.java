package ru.kotletkin.positiongen.result;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.positiongen.calculation.model.CalculationRequest;
import ru.kotletkin.positiongen.calculation.model.CalculationStatus;
import ru.kotletkin.positiongen.calculation.repository.CalculationRequestRepository;
import ru.kotletkin.positiongen.exception.CalculationNotReadyException;
import ru.kotletkin.positiongen.exception.StorageException;
import ru.kotletkin.positiongen.simulation.SimulationOutput;
import ru.kotletkin.positiongen.storage.ObjectStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

import static ru.kotletkin.positiongen.exception.NotFoundException.notFoundException;

@Service
@RequiredArgsConstructor
public class ResultArchiveService {

    private final ObjectStorageService objectStorage;
    private final CalculationRequestRepository requests;

    public StoredArtifacts saveToStore(final UUID calculationId, final SimulationOutput output) {
        String xyKey = "calculations/" + calculationId + "/XY.csv";
        String placesKey = "calculations/" + calculationId + "/PLACES.csv";
        objectStorage.put(xyKey, output.xyCsv(), "text/csv");
        objectStorage.put(placesKey, output.placesCsv(), "text/csv");
        try {
            return new StoredArtifacts(
                    xyKey,
                    placesKey,
                    Files.size(output.xyCsv()),
                    Files.size(output.placesCsv()),
                    output.reportCount());
        } catch (IOException ex) {
            throw new StorageException("Не удалось прочитать размер сохранённых файлов", ex);
        }
    }

    public StoredArtifacts getArtifactsInfo(final UUID calculationId) {
        CalculationResult result = readyResult(calculationId);
        return new StoredArtifacts(
                result.getXyObjectKey(),
                result.getPlacesObjectKey(),
                result.getXyByteSize(),
                result.getPlacesByteSize(),
                result.getReportCount());
    }

    public InputStream openFileByObjectKey(final String objectKey) {
        return objectStorage.open(objectKey);
    }

    private CalculationResult readyResult(final UUID calculationId) {
        CalculationRequest request = requests.findById(calculationId)
                .orElseThrow(notFoundException("Расчёт не найден"));
        if (request.getStatus() != CalculationStatus.SUCCEEDED || request.getResult() == null) {
            throw new CalculationNotReadyException("Расчёт ещё не готов");
        }
        return request.getResult();
    }
}
