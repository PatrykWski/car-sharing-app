package carsharing.app.dto.user;

import carsharing.app.model.RoleName;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private RoleName roleName;
}
