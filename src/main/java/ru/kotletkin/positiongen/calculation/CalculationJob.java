package ru.kotletkin.positiongen.calculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.kotletkin.positiongen.result.ResultArchiveService;
import ru.kotletkin.positiongen.result.StoredArtifacts;
import ru.kotletkin.positiongen.simulation.FractalRabbitCalculator;
import ru.kotletkin.positiongen.simulation.SimulationOutput;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationJob {

    private final CalculationService calculations;
    private final FractalRabbitCalculator calculator;
    private final ResultArchiveService results;

    private static String message(final Exception ex) {
        String text = ex.getMessage();
        if (text == null || text.isBlank()) {
            text = ex.getClass().getSimpleName();
        }
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    @Async("calculationExecutor")
    public void execute(final UUID uuid) {
        try {
            calculations.markRunning(uuid);
            try (SimulationOutput output = calculator.run(calculations.getSimulationRequestByUuid(uuid))) {
                StoredArtifacts artifacts = results.saveToStore(uuid, output);
                calculations.markSucceeded(uuid, artifacts);
            }
        } catch (Exception ex) {
            log.error("Calculation {} failed", uuid, ex);
            try {
                calculations.markFailed(uuid, message(ex));
            } catch (RuntimeException nested) {
                log.error("Could not store failure for {}", uuid, nested);
            }
        }
    }
}
