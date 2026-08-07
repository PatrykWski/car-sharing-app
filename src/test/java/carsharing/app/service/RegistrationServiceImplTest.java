package carsharing.app.service;

import static org.mockito.Mockito.when;

import carsharing.app.dto.register.UserRequestDto;
import carsharing.app.dto.register.UserResponseDto;
import carsharing.app.exception.UserExistException;
import carsharing.app.mapper.UserRegistrationMapper;
import carsharing.app.model.RoleName;
import carsharing.app.model.User;
import carsharing.app.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {
    private static final Long VALID_ID = 1L;
    private static final String VALID_EMAIL = "patryk@gmail.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRegistrationMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void register_ValidRequest_ReturnsDto() {
        //given
        User user = getUser();
        UserRequestDto userRequestDto = getUserRequestDto();
        UserResponseDto expected = getUserDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(userMapper.toModel(userRequestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(user.getPassword());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        //when
        UserResponseDto actual = registrationService.register(userRequestDto);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void register_UserAlreadyExists_ThrowUserExistException() {
        //given
        User user = getUser();
        UserRequestDto userRequestDto = getUserRequestDto();

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));

        //given & then
        Assertions.assertThrows(UserExistException.class,
                () -> registrationService.register(userRequestDto));
    }

    private User getUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setFirstName("Patryk");
        user.setLastName("Kowalski");
        user.setPassword("strongpassword");
        user.setRoleName(RoleName.CUSTOMER);
        return user;
    }

    private UserRequestDto getUserRequestDto() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(VALID_EMAIL);
        userRequestDto.setFirstName("Patryk");
        userRequestDto.setLastName("Kowalski");
        userRequestDto.setPassword("strongpassword");
        userRequestDto.setRepeatPassword("strongpassword");
        return userRequestDto;
    }

    private UserResponseDto getUserDto() {
        UserResponseDto userDto = new UserResponseDto();
        userDto.setRoleName(RoleName.CUSTOMER);
        userDto.setPassword("strongpassword");
        userDto.setEmail(VALID_EMAIL);
        userDto.setId(VALID_ID);
        userDto.setFirstName("Patryk");
        userDto.setLastName("Kowalski");
        return userDto;
    }
}

