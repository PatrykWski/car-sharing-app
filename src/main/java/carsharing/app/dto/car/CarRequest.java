package carsharing.app.dto.car;

import carsharing.app.model.TypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarRequest {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @NotNull
    private TypeName typeName;
    @PositiveOrZero
    private int inventory;
    @Positive
    private BigDecimal dailyFee;
}
