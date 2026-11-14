package ru.kotletkin.totleben.calculation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.kotletkin.totleben.calculation.CalculationLauncher;
import ru.kotletkin.totleben.calculation.CalculationService;
import ru.kotletkin.totleben.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.totleben.map.MapProperties;
import ru.kotletkin.totleben.simulation.FractalRabbitCalculator;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CalculationPageController {

    private final CalculationService calculations;
    private final CalculationLauncher launcher;
    private final FractalRabbitCalculator calculator;
    private final MapProperties mapProperties;

    @GetMapping("/")
    public String index(final Model model) {
        return list(model, new CalculationRequestDTO());
    }

    @PostMapping("/calculations")
    public String submit(@Valid @ModelAttribute("form") final CalculationRequestDTO form,
                         final BindingResult errors,
                         final Model model) {
        if (errors.hasErrors()) {
            return list(model, form);
        }
        UUID id = launcher.launch(form).id();
        return "redirect:/calculations/" + id + "/map";
    }

    @GetMapping("/calculations/{id}/map")
    public String map(@PathVariable final UUID id, final Model model) {
        model.addAttribute("calculation", calculations.get(id));
        model.addAttribute("originLatitude", mapProperties.originLatitude());
        model.addAttribute("originLongitude", mapProperties.originLongitude());
        model.addAttribute("maxPoints", mapProperties.maxPoints());
        model.addAttribute("constants", calculator.fixedParameters());
        return "calculations/map";
    }

    private String list(final Model model, final CalculationRequestDTO form) {
        model.addAttribute("form", form);
        model.addAttribute("requests", calculations.list());
        model.addAttribute("constants", calculator.fixedParameters());
        return "calculations/list";
    }
}
