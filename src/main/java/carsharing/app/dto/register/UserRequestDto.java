package carsharing.app.dto.register;

import carsharing.app.annotation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@FieldMatch(first = "password", second = "repeatPassword")
public class UserRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Min(value = 8)
    private String password;
    @NotBlank
    @Min(value = 8)
    private String repeatPassword;
}
