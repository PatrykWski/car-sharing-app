package carsharing.app.service.interfaces;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.model.RoleName;

public interface UserService {
    UserDto updateUserRole(Long id, RoleName roleName);

    UserDto getUser(String email);

    UserDto updateProfile(String email, UserUpdateDto userUpdateDto);
}
