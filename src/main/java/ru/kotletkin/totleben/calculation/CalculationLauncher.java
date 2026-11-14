package ru.kotletkin.totleben.calculation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.totleben.calculation.dto.CalculationDTO;
import ru.kotletkin.totleben.calculation.dto.CalculationRequestDTO;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalculationLauncher {

    private final CalculationService calculations;
    private final CalculationJob job;

    public CalculationDTO launch(final CalculationRequestDTO request) {
        UUID id = calculations.submit(request);
        job.execute(id);
        return calculations.get(id);
    }
}
