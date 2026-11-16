package ru.kotletkin.positiongen.calculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kotletkin.positiongen.calculation.CalculationLauncher;
import ru.kotletkin.positiongen.calculation.CalculationService;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/calculations")
@RequiredArgsConstructor
@Tag(name = "Calculations", description = "Calculation requests and their metadata")
public class CalculationController {

    private final CalculationService calculationService;
    private final CalculationLauncher calculationLauncher;

    @GetMapping
    @Operation(summary = "List calculations")
    public Page<CalculationDTO> getAllCalculations(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) final Pageable pageable) {
        return calculationService.getCalculations(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create calculation")
    public CalculationDTO createCalculation(@Valid @RequestBody final CalculationRequestDTO request) {
        return calculationLauncher.launch(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete calculation")
    public void deleteCalculationByUUID(@PathVariable(name = "id") final UUID uuid) {
        calculationService.deleteCalculationByUUID(uuid);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get calculation")
    public CalculationDTO getCalculation(@PathVariable final UUID id) {
        return calculationService.getCalculationByUuid(id);
    }
}
