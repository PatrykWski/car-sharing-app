package carsharing.app.mapper;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userToDto(User user);

    void updateUser(@MappingTarget User user, UserUpdateDto userUpdateDto);
}
