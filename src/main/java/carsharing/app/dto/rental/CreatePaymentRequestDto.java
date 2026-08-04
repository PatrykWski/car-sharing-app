package carsharing.app.dto.rental;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestDto(@NotNull Long rentalId) {
}
