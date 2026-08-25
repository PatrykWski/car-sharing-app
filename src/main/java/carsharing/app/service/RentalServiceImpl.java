package carsharing.app.service;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.dto.telegram.TelegramMessageRequest;
import carsharing.app.exception.AuthenticationException;
import carsharing.app.exception.EmptyInventoryException;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.NotificationError;
import carsharing.app.exception.RentalNotFinished;
import carsharing.app.mapper.RentalMapper;
import carsharing.app.model.Car;
import carsharing.app.model.Payment;
import carsharing.app.model.Rental;
import carsharing.app.model.StatusName;
import carsharing.app.model.User;
import carsharing.app.repository.CarRepository;
import carsharing.app.repository.PaymentRepository;
import carsharing.app.repository.RentalRepository;
import carsharing.app.repository.UserRepository;
import carsharing.app.service.interfaces.NotificationService;
import carsharing.app.service.interfaces.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private final RentalMapper rentalMapper;
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    @Value("${TELEGRAM_CHAT_ID}")
    private String chatId;

    @Override
    @Transactional
    public RentalDto addNewRental(String email, RentalRequestDto requestDto) {
        checkIfUserExist(email);
        User user = userRepository.findByEmail(email).get();

        validateNoPendingPayments(user.getId());

        Car car = getCar(requestDto.getCarId());

        if (car.getInventory() != 0) {
            Rental rental = rentalMapper.toModel(requestDto);
            car.setInventory(car.getInventory() - 1);
            carRepository.save(car);
            Rental savedRental = rentalRepository.save(rental);

            TelegramMessageRequest telegramMessageRequest = new TelegramMessageRequest(
                    chatId,
                    "New rental has been successfully created"
            );

            sendNotification(telegramMessageRequest);

            return rentalMapper.toDto(savedRental);
        }
        throw new EmptyInventoryException("There are no more cars available with id: "
                + car.getId() + " in the shop, choose another one");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalDto> getAllActualRentalsByUserId(Long userId, String email,
                                                       boolean isActive, Pageable pageable) {
        checkIfUserExist(userId, email);
        return rentalRepository.findRentalByActualReturnDate(userId, isActive, pageable)
                .map(rentalMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RentalDto getSpecificRentalById(Long id) {
        Rental rental = getRentalById(id);
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalDto setActualReturnDate(Long id) {
        Rental rental = getRentalById(id);
        rental.setActualReturnDate(LocalDate.now());
        Car car = getCar(rental.getCarId());
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        Rental savedRental = rentalRepository.save(rental);
        return rentalMapper.toDto(savedRental);
    }

    private Car getCar(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car with id: "
                        + id + " doesn't exist"));
    }

    private void checkIfUserExist(String email) {
        userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User does not exist"));
    }

    private void checkIfUserExist(Long id, String email) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User does not exist"));
        if (!user.getEmail().equals(email)) {
            throw new AuthenticationException("User with id: " + id + " doesn't exist");
        }
    }

    private Rental getRentalById(Long id) {
        return rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Rental with id: " + id + " doesn't exist"));
    }

    private void sendNotification(TelegramMessageRequest telegramMessageRequest) {
        String result = notificationService.sendNotification(telegramMessageRequest);
        if (result == null || result.isEmpty()) {
            throw new NotificationError("Result from notificationService"
                    + " was null or empty");
        }
    }

    private void validateNoPendingPayments(Long userId) {
        List<Rental> listOfRentals = rentalRepository.findRentalByUserId(userId);
        List<Long> listOfRentalsIds = listOfRentals.stream()
                .map(Rental::getId)
                .toList();

        if (listOfRentalsIds.isEmpty()) {
            return;
        }

        List<Payment> payments = paymentRepository.findAllByRentalIdIn(listOfRentalsIds);

        for (Payment p : payments) {
            if (p.getStatusName().equals(StatusName.PENDING)) {
                throw new RentalNotFinished("Can't rent new car before paying for previous one");
            }
        }
    }
}
