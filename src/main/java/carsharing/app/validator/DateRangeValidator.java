package carsharing.app.validator;

import carsharing.app.annotation.ValidDateRange;
import carsharing.app.dto.rental.RentalRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, RentalRequestDto> {

    @Override
    public boolean isValid(RentalRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getRentalDate() == null || dto.getReturnDate() == null) {
            return true;
        }

        return !dto.getReturnDate().isBefore(dto.getRentalDate());
    }
}