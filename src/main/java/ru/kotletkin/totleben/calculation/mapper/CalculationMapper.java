package ru.kotletkin.totleben.calculation.mapper;

import org.springframework.stereotype.Component;
import ru.kotletkin.totleben.calculation.dto.CalculationDTO;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;
import ru.kotletkin.totleben.result.model.CalculationResult;
import ru.kotletkin.totleben.simulation.SimulationRequest;

@Component
public class CalculationMapper {

    public CalculationDTO toDto(final CalculationRequest request) {
        CalculationResult result = request.getResult();
        return new CalculationDTO(
                request.getId(),
                request.getLabel(),
                request.getStatus(),
                request.getStatus().title(),
                request.getDimension(),
                request.getFractalDimension(),
                request.getPointCount(),
                request.getTravelerCount(),
                request.getCoTravelerCount(),
                request.getDays(),
                request.getMeanReportCount(),
                request.getCreatedAt(),
                request.getStartedAt(),
                request.getFinishedAt(),
                request.getErrorMessage(),
                result == null ? null : result.getReportCount(),
                result == null ? null : result.getXyByteSize(),
                result == null ? null : result.getPlacesByteSize());
    }

    public SimulationRequest toSimulationRequest(final CalculationRequest request) {
        return new SimulationRequest(
                request.getDimension(),
                request.getFractalDimension(),
                request.getPointCount(),
                request.getTravelerCount(),
                request.getCoTravelerCount(),
                request.getDays(),
                request.getMeanReportCount());
    }
}
