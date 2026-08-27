package carsharing.app.service.payment;

import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.model.Car;
import carsharing.app.model.PaymentType;
import carsharing.app.model.Rental;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.RentalRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeePayment implements PaymentMethod {
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("2.0");

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;

    @Override
    public BigDecimal totalToPay(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id: "
                        + rentalId + " doesnt exist"));

        Car car = carRepository.findById(rental.getCarId()).orElseThrow(
                () -> new EntityNotFoundException("Car with id: " + rental.getCarId()
                        + " doesnt exist"));

        long daysBetween = ChronoUnit.DAYS.between(rental.getReturnDate(),
                rental.getActualReturnDate());
        BigDecimal amount = car.getDailyFee().multiply(BigDecimal.valueOf(daysBetween));
        return amount.multiply(FINE_MULTIPLIER);
    }

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.FINE;
    }
}
