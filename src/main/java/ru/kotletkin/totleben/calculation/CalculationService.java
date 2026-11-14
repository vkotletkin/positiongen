package ru.kotletkin.totleben.calculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotletkin.totleben.calculation.dto.CalculationDTO;
import ru.kotletkin.totleben.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.totleben.calculation.mapper.CalculationMapper;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;
import ru.kotletkin.totleben.calculation.model.CalculationStatus;
import ru.kotletkin.totleben.calculation.repository.CalculationRequestRepository;
import ru.kotletkin.totleben.result.dto.StoredArtifacts;
import ru.kotletkin.totleben.result.model.CalculationResult;
import ru.kotletkin.totleben.simulation.SimulationRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static ru.kotletkin.totleben.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService {

    private final CalculationRequestRepository requests;
    private final CalculationMapper mapper;

    private static String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public UUID submit(final CalculationRequestDTO request) {
        CalculationRequest entity = new CalculationRequest();
        entity.setId(UUID.randomUUID());
        entity.setLabel(blankToNull(request.getLabel()));
        entity.setStatus(CalculationStatus.PENDING);
        entity.setDimension(request.getDimension());
        entity.setFractalDimension(request.getFractalDimension());
        entity.setPointCount(request.getPointCount());
        entity.setTravelerCount(request.getTravelerCount());
        entity.setCoTravelerCount(request.getCoTravelerCount());
        entity.setDays(request.getDays());
        entity.setMeanReportCount(request.getMeanReportCount());
        entity.setCreatedAt(Instant.now());
        requests.save(entity);
        log.info("Calculation {} queued", entity.getId());
        return entity.getId();
    }

    @Transactional(readOnly = true)
    public CalculationDTO get(final UUID id) {
        return mapper.toDto(load(id));
    }

    @Transactional(readOnly = true)
    public List<CalculationDTO> list() {
        return requests.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SimulationRequest simulationRequest(final UUID id) {
        return mapper.toSimulationRequest(load(id));
    }

    @Transactional
    public void markRunning(final UUID id) {
        CalculationRequest request = load(id);
        request.setStatus(CalculationStatus.RUNNING);
        request.setStartedAt(Instant.now());
    }

    @Transactional
    public void markSucceeded(final UUID id, final StoredArtifacts artifacts) {
        CalculationRequest request = load(id);
        request.setStatus(CalculationStatus.SUCCEEDED);
        request.setFinishedAt(Instant.now());
        request.setErrorMessage(null);
        CalculationResult result = new CalculationResult();
        result.setId(UUID.randomUUID());
        result.setRequest(request);
        result.setXyObjectKey(artifacts.xyObjectKey());
        result.setPlacesObjectKey(artifacts.placesObjectKey());
        result.setXyByteSize(artifacts.xyByteSize());
        result.setPlacesByteSize(artifacts.placesByteSize());
        result.setReportCount(artifacts.reportCount());
        result.setCreatedAt(Instant.now());
        request.setResult(result);
    }

    @Transactional
    public void markFailed(final UUID id, final String message) {
        CalculationRequest request = load(id);
        request.setStatus(CalculationStatus.FAILED);
        request.setFinishedAt(Instant.now());
        request.setErrorMessage(message);
    }

    private CalculationRequest load(final UUID id) {
        return requests.findById(id).orElseThrow(notFoundException("Расчёт не найден"));
    }
}
