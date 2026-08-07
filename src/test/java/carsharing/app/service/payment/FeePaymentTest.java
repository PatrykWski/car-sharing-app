package carsharing.app.service.payment;

import static org.mockito.Mockito.when;

import carsharing.app.model.Car;
import carsharing.app.model.Rental;
import carsharing.app.model.TypeName;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.RentalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FeePaymentTest {
    private static final Long VALID_ID = 1L;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Test
    void totalToPay_ValidRentalId_ReturnsTotalWithFine() {
        //given
        Rental rental = getRental();
        Car car = getCar();

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));

        FeePayment feePayment = new FeePayment(rentalRepository, carRepository);

        //when
        BigDecimal actual = feePayment.totalToPay(VALID_ID);

        //then
        Assertions.assertEquals(BigDecimal.valueOf(180.0), actual);

    }

    private Rental getRental() {
        Rental rental = new Rental();
        rental.setCarId(VALID_ID);
        rental.setActualReturnDate(LocalDate.now().plusDays(4));
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setId(VALID_ID);
        rental.setUserId(VALID_ID);
        return rental;
    }

    private Car getCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setInventory(20);
        car.setModel("Ibiza");
        car.setBrand("Seat");
        car.setTypeName(TypeName.SEDAN);
        car.setDailyFee(new BigDecimal(20));
        return car;
    }
}
