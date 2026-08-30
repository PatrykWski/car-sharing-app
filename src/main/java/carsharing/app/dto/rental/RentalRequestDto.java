package carsharing.app.dto.rental;

import carsharing.app.annotation.ValidDateRange;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
@ValidDateRange
public class RentalRequestDto {
    @NotNull
    @Positive
    private Long carId;
    @NotNull
    @FutureOrPresent
    private LocalDate rentalDate;
    @NotNull
    private LocalDate returnDate;
}
