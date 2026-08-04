package carsharing.app.service.interfaces;

import carsharing.app.dto.telegram.TelegramMessageRequest;

public interface NotificationService {
    String sendNotification(TelegramMessageRequest telegramMessageRequest);
}
