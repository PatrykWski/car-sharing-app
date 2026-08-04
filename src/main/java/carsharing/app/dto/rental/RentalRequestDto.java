package carsharing.app.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalRequestDto {
    @NotNull
    @Positive
    private Long carId;
    @NotNull
    @Positive
    private Long userId;
    @NotNull
    private LocalDate rentalDate;
    @NotNull
    private LocalDate returnDate;
}
