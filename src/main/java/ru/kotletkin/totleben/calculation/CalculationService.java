package ru.kotletkin.totleben.calculation;

import ru.kotletkin.totleben.calculation.dto.CalculationDTO;
import ru.kotletkin.totleben.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.totleben.result.dto.StoredArtifacts;
import ru.kotletkin.totleben.simulation.SimulationRequest;

import java.util.List;
import java.util.UUID;

public interface CalculationService {

    UUID submit(CalculationRequestDTO request);

    CalculationDTO get(UUID id);

    List<CalculationDTO> list();

    SimulationRequest simulationRequest(UUID id);

    void markRunning(UUID id);

    void markSucceeded(UUID id, StoredArtifacts artifacts);

    void markFailed(UUID id, String message);
}
