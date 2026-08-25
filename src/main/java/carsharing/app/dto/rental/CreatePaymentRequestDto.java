package carsharing.app.dto.rental;

import carsharing.app.model.PaymentType;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestDto(@NotNull Long rentalId, @NotNull PaymentType type) {
}
