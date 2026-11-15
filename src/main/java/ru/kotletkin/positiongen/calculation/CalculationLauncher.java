package ru.kotletkin.positiongen.calculation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalculationLauncher {

    private final CalculationService calculationService;
    private final CalculationJob calculationJob;

    public CalculationDTO launch(final CalculationRequestDTO request) {
        UUID id = calculationService.submit(request);
        calculationJob.execute(id);
        return calculationService.getCalculationByUuid(id);
    }
}
