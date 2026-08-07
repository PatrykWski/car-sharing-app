package carsharing.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
