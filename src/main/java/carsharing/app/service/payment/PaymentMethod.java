package carsharing.app.service.payment;

import java.math.BigDecimal;

public interface PaymentMethod {
    BigDecimal totalToPay(Long rentalId);
}
