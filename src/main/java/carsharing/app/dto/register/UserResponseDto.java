package carsharing.app.dto.register;

import carsharing.app.model.RoleName;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private RoleName roleName;
}
