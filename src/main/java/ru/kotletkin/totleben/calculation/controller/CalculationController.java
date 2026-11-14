package ru.kotletkin.totleben.calculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kotletkin.totleben.calculation.CalculationLauncher;
import ru.kotletkin.totleben.calculation.CalculationService;
import ru.kotletkin.totleben.calculation.dto.CalculationDTO;
import ru.kotletkin.totleben.calculation.dto.CalculationRequestDTO;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/calculations")
@RequiredArgsConstructor
@Tag(name = "Calculations", description = "Запросы на расчёт и их метаданные")
public class CalculationController {

    private final CalculationService calculations;
    private final CalculationLauncher launcher;

    @GetMapping
    @Operation(summary = "List calculations")
    public List<CalculationDTO> list() {
        return calculations.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create calculation")
    public CalculationDTO create(@Valid @RequestBody final CalculationRequestDTO request) {
        return launcher.launch(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get calculation")
    public CalculationDTO get(@PathVariable final UUID id) {
        return calculations.get(id);
    }
}
