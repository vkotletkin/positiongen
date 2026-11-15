package ru.kotletkin.positiongen.calculation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kotletkin.positiongen.calculation.CalculationLauncher;
import ru.kotletkin.positiongen.calculation.CalculationService;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.positiongen.config.MapProperties;
import ru.kotletkin.positiongen.simulation.FractalRabbitCalculator;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CalculationPageController {

    private static final int PAGE_SIZE = 20;

    private final CalculationService calculations;
    private final CalculationLauncher launcher;
    private final FractalRabbitCalculator calculator;
    private final MapProperties mapProperties;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", defaultValue = "0") final int page, final Model model) {
        return list(model, new CalculationRequestDTO(), page);
    }

    @PostMapping("/calculations")
    public String submit(@Valid @ModelAttribute("form") final CalculationRequestDTO form, final BindingResult errors, final Model model) {
        if (errors.hasErrors()) {
            return list(model, form, 0);
        }
        UUID id = launcher.launch(form).id();
        return "redirect:/calculations/%s/map".formatted(id);
    }

    @GetMapping("/calculations/{id}/map")
    public String map(@PathVariable final UUID id, final Model model) {
        model.addAttribute("calculation", calculations.getCalculationByUuid(id));
        model.addAttribute("originLatitude", mapProperties.originLatitude());
        model.addAttribute("originLongitude", mapProperties.originLongitude());
        model.addAttribute("maxPoints", mapProperties.maxPoints());
        model.addAttribute("constants", calculator.fixedParameters());
        return "calculations/map";
    }

    private String list(final Model model, final CalculationRequestDTO form, final int page) {
        Page<CalculationDTO> calculationPage = calculations.getCalculations(PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        if (page > 0 && calculationPage.getTotalPages() > 0 && page >= calculationPage.getTotalPages()) {
            calculationPage = calculations.getCalculations(PageRequest.of(calculationPage.getTotalPages() - 1, PAGE_SIZE));
        }
        model.addAttribute("form", form);
        model.addAttribute("requests", calculationPage.getContent());
        model.addAttribute("calculationPage", calculationPage);
        model.addAttribute("constants", calculator.fixedParameters());
        return "calculations/list";
    }
}
