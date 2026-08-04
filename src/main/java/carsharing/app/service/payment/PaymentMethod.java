package carsharing.app.service.payment;

import carsharing.app.model.PaymentType;
import java.math.BigDecimal;

public interface PaymentMethod {
    BigDecimal totalToPay(Long rentalId);

    PaymentType getSupportedType();
}
