package carsharing.app.dto.car;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateCarRequest {
    @PositiveOrZero
    private int inventory;
    @Positive
    private BigDecimal dailyFee;
}
