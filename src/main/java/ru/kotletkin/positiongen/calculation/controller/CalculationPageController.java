package ru.kotletkin.positiongen.calculation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.kotletkin.positiongen.calculation.CalculationLauncher;
import ru.kotletkin.positiongen.calculation.CalculationService;
import ru.kotletkin.positiongen.calculation.dto.CalculationDTO;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.positiongen.calculation.places.PlaceCoordinates;
import ru.kotletkin.positiongen.calculation.places.PlaceCsvReader;
import ru.kotletkin.positiongen.calculation.places.PlacePoint;
import ru.kotletkin.positiongen.config.MapProperties;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CalculationPageController {

    private static final int PAGE_SIZE = 20;
    private static final Set<String> GENERATED_ONLY = Set.of(
            "fractalDimension", "dimension", "pointCount", "restartRate");

    private final CalculationService calculations;
    private final CalculationLauncher launcher;
    private final MapProperties mapProperties;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", defaultValue = "0") final int page, final Model model) {
        return list(model, new CalculationRequestDTO(), page);
    }

    @PostMapping(value = "/calculations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String submit(@Valid @ModelAttribute("form") final CalculationRequestDTO form,
                         final BindingResult errors,
                         @RequestParam(name = "placesFile", required = false) final MultipartFile placesFile,
                         final Model model) {
        boolean uploaded = placesFile != null && !placesFile.isEmpty();
        if (uploaded) {
            try {
                List<PlacePoint> places = PlaceCsvReader.read(placesFile.getInputStream());
                if (places.isEmpty()) {
                    errors.rejectValue("places", "places", "Places file has no coordinates");
                } else {
                    PlaceCoordinates.toModelUnits(
                            places,
                            mapProperties.originLatitude(),
                            mapProperties.originLongitude(),
                            form.getKilometersPerUnit());
                    form.setPlaces(places);
                }
            } catch (IllegalArgumentException ex) {
                errors.rejectValue("places", "places", ex.getMessage());
            } catch (IOException ex) {
                errors.rejectValue("places", "places", "Could not read the places file");
            }
        }
        if (!canRun(errors, uploaded && form.getPlaces() != null)) {
            return list(model, form, 0);
        }
        UUID id = launcher.launch(form).id();
        return "redirect:/calculations/%s/map".formatted(id);
    }

    @PostMapping("/calculations/{id}/delete")
    public String delete(@PathVariable final UUID id, @RequestParam(name = "page", defaultValue = "0") final int page) {
        calculations.deleteCalculationByUUID(id);
        return "redirect:/?page=%d".formatted(Math.max(page, 0));
    }

    @GetMapping("/calculations/{id}/map")
    public String map(@PathVariable final UUID id, final Model model) {
        model.addAttribute("calculation", calculations.getCalculationByUuid(id));
        model.addAttribute("originLatitude", mapProperties.originLatitude());
        model.addAttribute("originLongitude", mapProperties.originLongitude());
        model.addAttribute("maxPoints", mapProperties.maxPoints());
        model.addAttribute("tileUrl", mapProperties.tileUrl());
        model.addAttribute("tileAttribution", mapProperties.tileAttribution());
        return "calculations/map";
    }

    private static boolean canRun(final BindingResult errors, final boolean uploaded) {
        if (!uploaded) {
            return !errors.hasErrors();
        }
        if (!errors.getGlobalErrors().isEmpty()) {
            return false;
        }
        for (FieldError error : errors.getFieldErrors()) {
            if (!GENERATED_ONLY.contains(error.getField())) {
                return false;
            }
        }
        return true;
    }

    private String list(final Model model, final CalculationRequestDTO form, final int page) {
        Page<CalculationDTO> calculationPage = calculations.getCalculations(PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        if (page > 0 && calculationPage.getTotalPages() > 0 && page >= calculationPage.getTotalPages()) {
            calculationPage = calculations.getCalculations(PageRequest.of(calculationPage.getTotalPages() - 1, PAGE_SIZE));
        }
        model.addAttribute("form", form);
        model.addAttribute("requests", calculationPage.getContent());
        model.addAttribute("calculationPage", calculationPage);
        return "calculations/list";
    }
}
