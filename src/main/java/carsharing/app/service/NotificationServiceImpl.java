package carsharing.app.service;

import carsharing.app.dto.telegram.TelegramMessageRequest;
import carsharing.app.exception.NotificationError;
import carsharing.app.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final RestClient restClient = RestClient.create();

    @Value("${telegram.token}")
    private String token;

    @Override
    public String sendNotification(TelegramMessageRequest telegramMessageRequest) {
        String url = "https://api.telegram.org/bot/" + token + "/sendMessage";

        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(telegramMessageRequest)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new NotificationError("Failed to send telegram notification" + e);
        }
    }
}
