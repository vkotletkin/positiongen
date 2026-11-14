package ru.kotletkin.totleben.result;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;
import ru.kotletkin.totleben.calculation.model.CalculationStatus;
import ru.kotletkin.totleben.calculation.repository.CalculationRequestRepository;
import ru.kotletkin.totleben.exception.CalculationNotReadyException;
import ru.kotletkin.totleben.exception.StorageException;
import ru.kotletkin.totleben.result.dto.StoredArtifacts;
import ru.kotletkin.totleben.result.model.CalculationResult;
import ru.kotletkin.totleben.simulation.SimulationOutput;
import ru.kotletkin.totleben.storage.ObjectStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

import static ru.kotletkin.totleben.exception.NotFoundException.notFoundException;

@Service
@RequiredArgsConstructor
public class ResultArchiveImpl implements ResultArchive {

    private final ObjectStorage objectStorage;
    private final CalculationRequestRepository requests;

    @Override
    public StoredArtifacts store(final UUID calculationId, final SimulationOutput output) {
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

    @Override
    public StoredArtifacts files(final UUID calculationId) {
        CalculationResult result = readyResult(calculationId);
        return new StoredArtifacts(
                result.getXyObjectKey(),
                result.getPlacesObjectKey(),
                result.getXyByteSize(),
                result.getPlacesByteSize(),
                result.getReportCount());
    }

    @Override
    public InputStream open(final String objectKey) {
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
