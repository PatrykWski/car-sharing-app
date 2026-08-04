package carsharing.app.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private Long carId;
    private Long userId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
}
