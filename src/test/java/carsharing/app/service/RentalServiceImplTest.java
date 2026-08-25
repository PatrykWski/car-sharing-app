package carsharing.app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.dto.telegram.TelegramMessageRequest;
import carsharing.app.exception.EmptyInventoryException;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.RentalNotFinished;
import carsharing.app.mapper.RentalMapper;
import carsharing.app.model.Car;
import carsharing.app.model.Payment;
import carsharing.app.model.PaymentType;
import carsharing.app.model.Rental;
import carsharing.app.model.RoleName;
import carsharing.app.model.StatusName;
import carsharing.app.model.TypeName;
import carsharing.app.model.User;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.PaymentRepository;
import carsharing.app.repository.RentalRepository;
import carsharing.app.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class RentalServiceImplTest {
    private static final String VALID_EMAIL = "patrykw@gmail.com";
    private static final String INVALID_EMAIL = "patryk123";
    private static final boolean RENTAL_IS_ACTIVE = true; //Rental actual return date = null
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    void addNewRental_ValidRequestDto_ReturnsRentalDto() {
        //given
        User user = getUser();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        Rental rental = getRental();
        List<Rental> listOfRentals = List.of(rental);
        when(rentalRepository.findRentalByUserId(user.getId())).thenReturn(listOfRentals);
        List<Long> listOfRentalIds = listOfRentals.stream()
                .map(Rental::getId)
                .toList();
        Payment payment = getPayment();
        List<Payment> payments = List.of(payment);
        when(paymentRepository.findAllByRentalIdIn(listOfRentalIds)).thenReturn(payments);
        Car car = getCar();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        RentalRequestDto requestDto = getRequestDto();
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        Car minusQuantity = getCar();
        minusQuantity.setInventory(minusQuantity.getInventory() - 1);
        when(carRepository.save(car)).thenReturn(minusQuantity);
        when(rentalRepository.save(rental)).thenReturn(rental);
        RentalDto expected = getRentalDto();
        when(rentalMapper.toDto(rental)).thenReturn(expected);
        when(notificationService.sendNotification(any(TelegramMessageRequest.class)))
                .thenReturn("OK");

        //when
        RentalDto actual = rentalService.addNewRental(VALID_EMAIL, requestDto);

        //then
        Assertions.assertEquals(expected, actual);
        verify(notificationService, times(1)).sendNotification(any(TelegramMessageRequest.class));
    }

    @Test
    void addNewRental_InvalidUserEmail_ThrowsNotFound() {
        //given
        User user = getUser();
        user.setId(VALID_ID);
        RentalRequestDto requestDto = getRequestDto();
        when(userRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.of(user));

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.addNewRental(INVALID_EMAIL, requestDto));
    }

    @Test
    void addNewRental_UserWithoutRentals_ReturnsRentalDto() {
        //given
        User user = getUser();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        List<Rental> listOfRentals = List.of();
        when(rentalRepository.findRentalByUserId(user.getId())).thenReturn(listOfRentals);
        Car car = getCar();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        RentalRequestDto requestDto = getRequestDto();
        Rental rental = getRental();
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        Car minusQuantity = getCar();
        minusQuantity.setInventory(minusQuantity.getInventory() - 1);
        when(carRepository.save(car)).thenReturn(minusQuantity);
        when(rentalRepository.save(rental)).thenReturn(rental);
        RentalDto expected = getRentalDto();
        when(rentalMapper.toDto(rental)).thenReturn(expected);
        when(notificationService.sendNotification(any(TelegramMessageRequest.class)))
                .thenReturn("OK");

        //when
        RentalDto actual = rentalService.addNewRental(VALID_EMAIL, requestDto);

        //then
        Assertions.assertEquals(expected, actual);
        verify(notificationService, times(1)).sendNotification(any(TelegramMessageRequest.class));
    }

    @Test
    void addNewRental_UserWithPendingPayments_ThrowsRentalNotFinished() {
        //given
        User user = getUser();
        Rental rental = getRental();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        List<Rental> listOfRentals = List.of(rental);
        when(rentalRepository.findRentalByUserId(user.getId())).thenReturn(listOfRentals);
        Payment payment = getPayment();
        payment.setStatusName(StatusName.PENDING);
        List<Payment> payments = List.of(payment);
        List<Long> listOfRentalIds = listOfRentals.stream()
                .map(Rental::getId)
                .toList();
        when(paymentRepository.findAllByRentalIdIn(listOfRentalIds)).thenReturn(payments);
        RentalRequestDto requestDto = getRequestDto();

        //when & then
        Assertions.assertThrows(RentalNotFinished.class,
                () -> rentalService.addNewRental(VALID_EMAIL, requestDto));
    }

    @Test
    void addNewRental_CarDoesNotExist_ThrowsEntityNotFoundException() {
        //given
        User user = getUser();
        RentalRequestDto requestDto = getRequestDto();
        requestDto.setCarId(INVALID_ID);

        Rental rental = getRental();
        List<Rental> listOfRentals = List.of(rental);
        List<Long> listOfRentalIds = listOfRentals.stream()
                .map(Rental::getId)
                .toList();
        Payment payment = getPayment();
        List<Payment> payments = List.of(payment);

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findRentalByUserId(user.getId())).thenReturn(listOfRentals);
        when(paymentRepository.findAllByRentalIdIn(listOfRentalIds)).thenReturn(payments);
        when(carRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.addNewRental(VALID_EMAIL, requestDto));
    }

    @Test
    void addNewRental_EmptyCarInventory_EmptyInventoryException() {
        //given
        User user = getUser();
        Rental rental = getRental();
        List<Rental> listOfRentals = List.of(rental);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findRentalByUserId(user.getId())).thenReturn(listOfRentals);
        List<Long> listOfRentalIds = listOfRentals.stream()
                .map(Rental::getId)
                .toList();
        Payment payment = getPayment();
        List<Payment> payments = List.of(payment);
        when(paymentRepository.findAllByRentalIdIn(listOfRentalIds)).thenReturn(payments);
        Car car = getCar();
        car.setInventory(0);
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        RentalRequestDto requestDto = getRequestDto();

        //when & then
        Assertions.assertThrows(EmptyInventoryException.class,
                () -> rentalService.addNewRental(VALID_EMAIL, requestDto));
    }

    @Test
    void getAllActualRentalsByUserId_RentalsExist_ReturnRentalDtoPage() {
        //given
        User user = getUser();
        Pageable pageable = PageRequest.of(0, 10);
        Rental rental = getRental();
        RentalDto rentalDto = getRentalDto();
        Page<Rental> page = new PageImpl<>(List.of(rental), pageable, 1);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findRentalByActualReturnDate(VALID_ID, RENTAL_IS_ACTIVE, pageable))
                .thenReturn(page);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        //when
        Page<RentalDto> actual = rentalService.getAllActualRentalsByUserId(
                VALID_ID, VALID_EMAIL, RENTAL_IS_ACTIVE, pageable);

        //then
        Page<RentalDto> expected = new PageImpl<>(List.of(rentalDto), pageable, 1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getAllActualRentalsByUserId_UserDoesNotExist_ThrowEntityNotFoundException() {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.getAllActualRentalsByUserId(
                        INVALID_ID, VALID_EMAIL, RENTAL_IS_ACTIVE, pageable));
    }

    @Test
    void getSpecificRentalById_RentalExist_ReturnsRentalDto() {
        //given
        Rental rental = getRental();
        User user = getUser();
        RentalDto expected = getRentalDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        //when
        RentalDto actual = rentalService.getSpecificRentalById(VALID_ID, VALID_EMAIL);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getSpecificRentalById_RentalDoesNotExist_ReturnsEntityNotFoundException() {
        //given
        User user = getUser();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.getSpecificRentalById(INVALID_ID, VALID_EMAIL));
    }

    @Test
    void setActualReturnDate_RentalExist_ReturnsRentalDto() {
        //given
        Rental rental = getRental();
        RentalDto expected = getRentalDto();
        Car car = getCar();

        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        //when
        RentalDto actual = rentalService.setActualReturnDate(VALID_ID);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void setActualReturnDate_RentalDoesNotExist_ThrowsEntityNotFoundException() {
        //given
        when(rentalRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.setActualReturnDate(INVALID_ID));
    }

    @Test
    void setActualReturnDate_RentalActualReturnDateIsNotNull_ThrowsEntityNotFoundException() {
        //given
        Rental rental = getRental();
        rental.setActualReturnDate(LocalDate.now());
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.setActualReturnDate(VALID_ID));
    }

    @Test
    void setActualReturnDate_CarDoesntExist_ThrowsEntityNotFoundException() {
        //given
        Rental rental = getRental();
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(rental));
        when(carRepository.findById(rental.getCarId())).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> rentalService.setActualReturnDate(VALID_ID));
    }

    private RentalDto getRentalDto() {
        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(VALID_ID);
        rentalDto.setRentalDate(LocalDate.now());
        rentalDto.setActualReturnDate(null);
        rentalDto.setUserId(VALID_ID);
        rentalDto.setReturnDate(LocalDate.now().plusDays(5));
        rentalDto.setCarId(VALID_ID);
        return rentalDto;
    }

    private Rental getRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(LocalDate.now());
        rental.setActualReturnDate(null);
        rental.setUserId(VALID_ID);
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setCarId(VALID_ID);
        return rental;
    }

    private RentalRequestDto getRequestDto() {
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(VALID_ID);
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(5));
        requestDto.setCarId(VALID_ID);
        return requestDto;
    }

    private User getUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setFirstName("Patryk");
        user.setLastName("Wisz");
        user.setPassword(passwordEncoder.encode("strongpassword"));
        user.setRoleName(RoleName.CUSTOMER);
        return user;
    }

    private Payment getPayment() {
        Payment payment = new Payment();
        payment.setRentalId(VALID_ID);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setStatusName(StatusName.PAID);
        payment.setAmountToPay(new BigDecimal(100));
        payment.setId(VALID_ID);
        return payment;
    }

    private Car getCar() {
        Car car = new Car();
        car.setInventory(10);
        car.setTypeName(TypeName.SEDAN);
        car.setModel("Ibiza");
        car.setBrand("Seat");
        car.setDailyFee(new BigDecimal(30));
        car.setId(VALID_ID);
        return car;
    }
}
