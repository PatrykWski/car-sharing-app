package carsharing.app.annotation;

import carsharing.app.validator.DateRangeValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "Return date must be after or equal to rental date";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}