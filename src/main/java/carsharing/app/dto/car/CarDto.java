package carsharing.app.dto.car;

import carsharing.app.model.TypeName;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private TypeName typeName;
    private int inventory;
    private BigDecimal dailyFee;
}
