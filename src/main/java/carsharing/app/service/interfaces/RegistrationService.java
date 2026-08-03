package carsharing.app.service.interfaces;

import carsharing.app.dto.register.UserRequestDto;
import carsharing.app.dto.register.UserResponseDto;

public interface RegistrationService {
    UserResponseDto register(UserRequestDto userRequestDto);
}
