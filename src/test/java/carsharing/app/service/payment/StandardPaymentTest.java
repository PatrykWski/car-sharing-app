package carsharing.app.service.payment;

import static org.mockito.Mockito.when;

import carsharing.app.exception.EntityNotFoundException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StandardPaymentTest {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private StandardPayment standardPayment;

    @Test
    void totalToPay_ValidRentalId_ReturnsTotal() {
        //given
        Rental rental = getRental();
        Car car = getCar();
        BigDecimal expected = new BigDecimal(80);
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));

        //when
        BigDecimal actual = standardPayment.totalToPay(VALID_ID);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void totalToPay_InvalidRentalId_ReturnsEntityNotFoundException() {
        //given
        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> standardPayment.totalToPay(INVALID_ID));
    }

    @Test
    void totalToPay_InvalidCarId_ReturnsEntityNotFoundException() {
        //given
        Rental rental = getRental();
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> standardPayment.totalToPay(VALID_ID));
    }

    @Test
    void totalToPay_RentalAndReturnSameDay_ReturnsOneDayPayment() {
        //given
        Rental rental = getRental();
        rental.setActualReturnDate(LocalDate.now());
        Car car = getCar();
        BigDecimal expected = new BigDecimal(20);
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));

        //when
        BigDecimal actual = standardPayment.totalToPay(VALID_ID);

        // & then
        Assertions.assertEquals(expected, actual);
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
