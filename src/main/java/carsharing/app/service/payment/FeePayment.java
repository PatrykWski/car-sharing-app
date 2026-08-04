package carsharing.app.service.payment;

import carsharing.app.model.PaymentType;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.RentalRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FeePayment extends StandardPayment {
    private static final BigDecimal FINE_AMOUNT = BigDecimal.valueOf(100.00);

    public FeePayment(RentalRepository rentalRepository, CarRepository carRepository) {
        super(rentalRepository, carRepository);
    }

    @Override
    public BigDecimal totalToPay(Long rentalId) {
        BigDecimal standardTotal = super.totalToPay(rentalId);
        return standardTotal.add(FINE_AMOUNT);
    }

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.FINE;
    }
}
