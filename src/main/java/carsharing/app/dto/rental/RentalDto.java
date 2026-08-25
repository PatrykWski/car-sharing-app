package carsharing.app.dto.rental;

import carsharing.app.model.TypeName;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private Long carId;
    private String model;
    private String brand;
    private TypeName typeName;
    private BigDecimal dailyFee;
    private Long userId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
}
