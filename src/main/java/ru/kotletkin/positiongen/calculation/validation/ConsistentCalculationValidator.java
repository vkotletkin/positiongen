package ru.kotletkin.positiongen.calculation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.kotletkin.positiongen.calculation.dto.CalculationRequestDTO;

public class ConsistentCalculationValidator implements ConstraintValidator<ConsistentCalculation, CalculationRequestDTO> {

    @Override
    public boolean isValid(final CalculationRequestDTO form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        if (form.getFractalDimension() >= form.getDimension()) {
            context.buildConstraintViolationWithTemplate(
                            "Fractal dimension must be less than the space dimension")
                    .addPropertyNode("fractalDimension")
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
        return valid;
    }
}
