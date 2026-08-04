package carsharing.app.mapper;

import carsharing.app.dto.register.UserRequestDto;
import carsharing.app.dto.register.UserResponseDto;
import carsharing.app.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRegistrationMapper {
    UserResponseDto toDto(User user);

    User toModel(UserRequestDto userRequestDto);
}
