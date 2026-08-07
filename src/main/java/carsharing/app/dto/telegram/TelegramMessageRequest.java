package carsharing.app.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TelegramMessageRequest(
        @NotBlank @JsonProperty("chat_id") String chatId,
        @NotBlank String text) {
}
