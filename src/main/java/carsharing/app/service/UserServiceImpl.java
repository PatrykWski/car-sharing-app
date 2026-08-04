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
import org.springframework.security.core.userdetails.UserDetails;
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
        user.setRoleName(roleName);
        User savedUser = userRepository.save(user);
        return userMapper.userToDto(savedUser);
    }

    @Override
    public UserDto getUser(UserDetails userDetails) {
        return userMapper.userToDto(getUserByUserDetails(userDetails));
    }

    @Override
    public UserDto updateProfile(UserDetails userDetails, UserUpdateDto userUpdateDto) {
        User user = getUserByUserDetails(userDetails);
        userMapper.updateUser(user, userUpdateDto);
        User savedUser = userRepository.save(user);
        return userMapper.userToDto(savedUser);
    }

    private User getUserByUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("Cannot find user by this email"));
    }
}
