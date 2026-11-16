package ru.kotletkin.positiongen.calculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.positiongen.calculation.mapper.CalculationMapper;
import ru.kotletkin.positiongen.calculation.model.CalculationRequest;
import ru.kotletkin.positiongen.calculation.model.CalculationStatus;
import ru.kotletkin.positiongen.calculation.repository.CalculationRequestRepository;
import ru.kotletkin.positiongen.result.CalculationResult;
import ru.kotletkin.positiongen.result.StoredArtifacts;
import ru.kotletkin.positiongen.simulation.SimulationRequest;
import ru.kotletkin.positiongen.storage.ObjectStorageService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.kotletkin.positiongen.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalculationService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final CalculationRequestRepository requests;
    private final CalculationMapper mapper;
    private final ObjectStorageService objectStorage;

    @Transactional
    public UUID submit(final CalculationRequestDTO request) {
        CalculationRequest entity = new CalculationRequest();
        entity.setId(UUID.randomUUID());
        entity.setLabel(StringUtils.trimToEmpty(request.getLabel()));
        entity.setStatus(CalculationStatus.PENDING);
        entity.setDimension(request.getDimension());
        entity.setFractalDimension(request.getFractalDimension());
        entity.setPointCount(request.getPointCount());
        entity.setTravelerCount(request.getTravelerCount());
        entity.setCoTravelerCount(request.getCoTravelerCount());
        entity.setDays(request.getDays());
        entity.setMeanReportCount(request.getMeanReportCount());
        entity.setRestartRate(request.getRestartRate());
        entity.setDistanceExponent(request.getDistanceExponent());
        entity.setMaxKilometersPerHour(request.getMaxKilometersPerHour());
        entity.setKilometersPerUnit(request.getKilometersPerUnit());
        entity.setPhiMean(request.getPhiMean());
        entity.setStepsMean(request.getStepsMean());
        entity.setReportDelta(request.getReportDelta());
        entity.setReportAlpha(request.getReportAlpha());
        entity.setCreatedAt(Instant.now());
        requests.save(entity);
        log.info("Calculation {} queued", entity.getId());
        return entity.getId();
    }

    @Transactional
    public void deleteCalculationByUUID(final UUID uuid) {
        CalculationRequest request = loadCalculationByUuid(uuid);
        CalculationResult result = request.getResult();
        if (result != null) {
            objectStorage.delete(result.getXyObjectKey());
            objectStorage.delete(result.getPlacesObjectKey());
        }
        requests.delete(request);
    }

    public CalculationDTO getCalculationByUuid(final UUID id) {
        return mapper.toDto(loadCalculationByUuid(id));
    }

    public Page<CalculationDTO> getCalculations(final Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : NEWEST_FIRST;
        Pageable newestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return requests.findAll(newestFirst).map(mapper::toDto);
    }

    public SimulationRequest getSimulationRequestByUuid(final UUID id) {
        return mapper.toSimulationRequest(loadCalculationByUuid(id));
    }

    @Transactional
    public void markRunning(final UUID id) {
        CalculationRequest request = loadCalculationByUuid(id);
        request.setStatus(CalculationStatus.RUNNING);
        request.setStartedAt(Instant.now());
    }

    @Transactional
    public void markSucceeded(final UUID id, final StoredArtifacts artifacts, final List<Integer> coTravelerIds) {
        CalculationRequest request = loadCalculationByUuid(id);
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
        result.setCoTravelerIds(new ArrayList<>(coTravelerIds));
        result.setCreatedAt(Instant.now());
        request.setResult(result);
    }

    @Transactional
    public void markFailed(final UUID id, final String message) {
        CalculationRequest request = loadCalculationByUuid(id);
        request.setStatus(CalculationStatus.FAILED);
        request.setFinishedAt(Instant.now());
        request.setErrorMessage(message);
    }

    private CalculationRequest loadCalculationByUuid(final UUID id) {
        return requests.findById(id).orElseThrow(notFoundException("Calculation with id {0} not found", id));
    }
}
