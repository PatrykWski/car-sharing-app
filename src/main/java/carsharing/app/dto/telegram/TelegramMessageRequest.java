package carsharing.app.dto.telegram;

import jakarta.validation.constraints.NotBlank;

public record TelegramMessageRequest(@NotBlank String chatId, @NotBlank String text) {
}
