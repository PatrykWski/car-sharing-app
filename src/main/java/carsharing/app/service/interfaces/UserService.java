package carsharing.app.service.interfaces;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.model.RoleName;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDto updateUserRole(Long id, RoleName roleName);

    UserDto getUser(UserDetails userDetails);

    UserDto updateProfile(UserDetails userDetails, UserUpdateDto userUpdateDto);
}
