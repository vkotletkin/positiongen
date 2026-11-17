package ru.kotletkin.positiongen.calculation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;
import ru.kotletkin.positiongen.calculation.places.PlaceCoordinates;
import ru.kotletkin.positiongen.config.MapProperties;

public class ConsistentCalculationValidator implements ConstraintValidator<ConsistentCalculation, CalculationRequestDTO> {

    private final MapProperties mapProperties;

    public ConsistentCalculationValidator(final MapProperties mapProperties) {
        this.mapProperties = mapProperties;
    }

    @Override
    public boolean isValid(final CalculationRequestDTO form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        boolean valid = true;
        boolean uploaded = form.getPlaces() != null && !form.getPlaces().isEmpty();
        context.disableDefaultConstraintViolation();
        if (!uploaded && form.getFractalDimension() >= form.getDimension()) {
            context.buildConstraintViolationWithTemplate(
                            "Fractal dimension must be less than the space dimension")
                    .addPropertyNode("fractalDimension")
                    .addConstraintViolation();
            valid = false;
        }
        if (form.isTravelTogether() && form.getCoTravelerCount() < 2) {
            context.buildConstraintViolationWithTemplate(
                            "Traveling together needs at least two companions")
                    .addPropertyNode("travelTogether")
                    .addConstraintViolation();
            valid = false;
        }
        if (form.getCoTravelerCount() > form.getTravelerCount()) {
            context.buildConstraintViolationWithTemplate(
                            "Co-traveler count cannot exceed traveler count")
                    .addPropertyNode("coTravelerCount")
                    .addConstraintViolation();
            valid = false;
        }
        if (uploaded) {
            try {
                PlaceCoordinates.toModelUnits(
                        form.getPlaces(),
                        mapProperties.originLatitude(),
                        mapProperties.originLongitude(),
                        form.getKilometersPerUnit());
            } catch (IllegalArgumentException ex) {
                context.buildConstraintViolationWithTemplate(ex.getMessage())
                        .addPropertyNode("places")
                        .addConstraintViolation();
                valid = false;
            }
        }
        return valid;
    }
}
