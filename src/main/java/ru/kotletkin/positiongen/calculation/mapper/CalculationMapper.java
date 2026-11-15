package ru.kotletkin.positiongen.calculation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.model.CalculationRequest;
import ru.kotletkin.positiongen.calculation.model.CalculationStatus;
import ru.kotletkin.positiongen.simulation.SimulationRequest;

@Mapper(componentModel = "spring")
public interface CalculationMapper {

    @Mapping(target = "statusTitle", source = "status", qualifiedByName = "statusTitle")
    @Mapping(target = "reportCount", source = "result.reportCount")
    @Mapping(target = "xyByteSize", source = "result.xyByteSize")
    @Mapping(target = "placesByteSize", source = "result.placesByteSize")
    CalculationDTO toDto(CalculationRequest request);

    SimulationRequest toSimulationRequest(CalculationRequest request);

    @Named("statusTitle")
    default String statusTitle(final CalculationStatus status) {
        return status == null ? null : status.getTitle();
    }
}
