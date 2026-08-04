package carsharing.app.service.payment;

import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.RentalNotFinished;
import carsharing.app.model.Car;
import carsharing.app.model.Rental;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.RentalRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StandardPayment implements PaymentMethod {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;

    @Override
    public BigDecimal totalToPay(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id: "
                        + rentalId + " doesnt exist"));

        if (rental.getActualReturnDate() == null) {
            throw new RentalNotFinished("Rental have to be finished, actual return date is null");
        }

        Car car = carRepository.findById(rental.getCarId()).orElseThrow(
                () -> new EntityNotFoundException("Car with id: " + rental.getCarId()
                        + " doesnt exist"));

        long daysBetween = ChronoUnit.DAYS.between(rental.getRentalDate(),
                rental.getActualReturnDate());

        if (daysBetween == 0) {
            daysBetween++;
        }

        return new BigDecimal(daysBetween).multiply(car.getDailyFee());
    }
}
