package carsharing.app.scheduler;

import carsharing.app.dto.telegram.TelegramMessageRequest;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.NotificationError;
import carsharing.app.model.Rental;
import carsharing.app.model.User;
import carsharing.app.repository.RentalRepository;
import carsharing.app.repository.UserRepository;
import carsharing.app.service.interfaces.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalCheckScheduler {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${telegram.chatId}")
    private String chatId;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkIfRentalReturnDateIsNotLate() {
        List<Rental> rentals = rentalRepository.findAll();
        List<Rental> listOfLateRentals = rentals.stream()
                .filter(rental -> rental.getReturnDate().isBefore(LocalDate.now()))
                .filter(rental -> rental.getActualReturnDate() == null)
                .toList();
        if (!listOfLateRentals.isEmpty()) {
            sendNotificationWhenLate(listOfLateRentals);
        } else {
            TelegramMessageRequest telegramMessageRequest = new TelegramMessageRequest(
                    chatId, "No rentals overdue today!");
            sendNotification(telegramMessageRequest);
        }
    }

    private void sendNotificationWhenLate(List<Rental> list) {
        for (Rental r : list) {
            User user = userRepository.findById(r.getUserId()).orElseThrow(
                    () -> new EntityNotFoundException("User with id: " + r.getUserId()
                            + "doesn't exist"));
            TelegramMessageRequest telegramMessageRequest = new TelegramMessageRequest(
                    chatId,
                    user.getFirstName() + " " + user.getLastName() + " " + user.getEmail()
                            + " is late with returning a car. "
                            + "Call us immediately so we can solve the problem."
                            + " Your rental id is: " + r.getId()

            );
            sendNotification(telegramMessageRequest);
        }
    }

    private void sendNotification(TelegramMessageRequest telegramMessageRequest) {
        String result = notificationService.sendNotification(telegramMessageRequest);
        if (result == null || result.isEmpty()) {
            throw new NotificationError("Result from notificationService"
                    + " was null or empty");
        }
    }
}
