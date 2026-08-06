package carsharing.app.service;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.mapper.UserMapper;
import carsharing.app.model.RoleName;
import carsharing.app.model.User;
import carsharing.app.repository.UserRepository;
import carsharing.app.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto updateUserRole(Long id, RoleName roleName) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User with id: " + id + " doesn't exist"));

        if (roleName == null) {
            throw new EntityNotFoundException("Role can't be null");
        }

        user.setRoleName(roleName);
        User savedUser = userRepository.save(user);
        return userMapper.userToDto(savedUser);
    }

    @Override
    public UserDto getUser(String email) {
        return userMapper.userToDto(getUserByUserDetails(email));
    }

    @Override
    public UserDto updateProfile(String email, UserUpdateDto userUpdateDto) {
        User user = getUserByUserDetails(email);
        userMapper.updateUser(user, userUpdateDto);
        User savedUser = userRepository.save(user);
        return userMapper.userToDto(savedUser);
    }

    private User getUserByUserDetails(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Cannot find user by this email"));
    }
}
