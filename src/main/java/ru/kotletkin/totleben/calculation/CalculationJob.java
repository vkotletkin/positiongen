package ru.kotletkin.totleben.calculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.result.ResultArchive;
import ru.kotletkin.totleben.result.dto.StoredArtifacts;
import ru.kotletkin.totleben.simulation.FractalRabbitCalculator;
import ru.kotletkin.totleben.simulation.SimulationOutput;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationJob {

    private final CalculationService calculations;
    private final FractalRabbitCalculator calculator;
    private final ResultArchive results;

    @Async("calculationExecutor")
    public void execute(final UUID id) {
        try {
            calculations.markRunning(id);
            try (SimulationOutput output = calculator.run(calculations.simulationRequest(id))) {
                StoredArtifacts artifacts = results.store(id, output);
                calculations.markSucceeded(id, artifacts);
            }
        } catch (Exception ex) {
            log.error("Calculation {} failed", id, ex);
            try {
                calculations.markFailed(id, message(ex));
            } catch (RuntimeException nested) {
                log.error("Could not store failure for {}", id, nested);
            }
        }
    }

    private static String message(final Exception ex) {
        String text = ex.getMessage();
        if (text == null || text.isBlank()) {
            text = ex.getClass().getSimpleName();
        }
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }
}
