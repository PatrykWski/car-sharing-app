package carsharing.app.service.payment;

import carsharing.app.model.Car;
import carsharing.app.model.PaymentType;
import carsharing.app.model.Rental;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.RentalRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class FeePayment extends StandardPayment {
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("2.0");

    public FeePayment(RentalRepository rentalRepository, CarRepository carRepository) {
        super(rentalRepository, carRepository);
    }

    @Override
    public BigDecimal totalToPay(Long rentalId) {
        BigDecimal standardTotal = super.totalToPay(rentalId);

        Rental rental = rentalRepository.findById(rentalId).get();
        Car car = carRepository.findById(rental.getCarId()).get();

        if (rental.getActualReturnDate().isAfter(rental.getReturnDate())) {
            long daysBetween = ChronoUnit.DAYS.between(rental.getReturnDate(),
                    rental.getActualReturnDate());
            BigDecimal amount = car.getDailyFee().multiply(BigDecimal.valueOf(daysBetween));
            BigDecimal total = amount.multiply(FINE_MULTIPLIER);
            return standardTotal.add(total);
        }
        return standardTotal;
    }

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.FINE;
    }
}
