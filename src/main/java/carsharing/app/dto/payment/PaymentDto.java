package carsharing.app.dto.payment;

import carsharing.app.model.PaymentType;
import carsharing.app.model.StatusName;
import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;

@Data
public class PaymentDto {
    private Long id;
    private StatusName statusName;
    private PaymentType paymentType;
    private Long rentalId;
    private URL url;
    private String sessionId;
    private BigDecimal amountToPay;
}
