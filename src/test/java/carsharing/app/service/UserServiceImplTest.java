package carsharing.app.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.mapper.UserMapper;
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

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String VALID_EMAIL = "patrykK@gmail.com";
    private static final String INVALID_EMAIL = "patrykK";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUserRole_ParamsAreValid_ReturnsUserDto() {
        //given
        User user = getUser();
        RoleName roleName = RoleName.CUSTOMER;
        UserDto expected = getUserDto();
        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToDto(user)).thenReturn(expected);

        //when
        UserDto actual = userService.updateUserRole(VALID_ID, roleName);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void updateUserRole_IdIsNotValid_ThrowsEntityNotFound() {
        //given
        RoleName roleName = RoleName.CUSTOMER;
        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(INVALID_ID, roleName));
    }

    @Test
    void updateUserRole_RoleIsNull_ThrowsEntityNotFound() {
        //given
        RoleName roleName = null;

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(VALID_ID, roleName));
    }

    @Test
    void getUser_ValidEmail_ReturnsUserDto() {
        //given
        User user = getUser();
        UserDto expected = getUserDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.userToDto(user)).thenReturn(expected);

        //when
        UserDto actual = userService.getUser(VALID_EMAIL);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getUser_InvalidEmail_ReturnsUserDto() {
        //given
        when(userRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> userService.getUser(INVALID_EMAIL));
    }

    @Test
    void updateProfile_ValidParams_ReturnsUserDto() {
        //given
        User user = getUser();
        UserDto expected = getUserDto();
        expected.setFirstName("Przemek");
        expected.setLastName("Kowalski");
        UserUpdateDto userUpdateDto = getUserUpdate();

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUser(user, userUpdateDto);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToDto(user)).thenReturn(expected);

        //when
        UserDto actual = userService.updateProfile(VALID_EMAIL, userUpdateDto);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void updateProfile_InvalidEmail_ThrowsEntityNotFound() {
        //given
        UserUpdateDto userUpdateDto = getUserUpdate();
        when(userRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> userService.updateProfile(INVALID_EMAIL, userUpdateDto));
    }

    private User getUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setLastName("Kowalski");
        user.setFirstName("Patryk");
        user.setPassword("strongpassword");
        user.setRoleName(RoleName.MANAGER);
        user.setEmail(VALID_EMAIL);
        return user;
    }

    private UserDto getUserDto(){
        UserDto userDto = new UserDto();
        userDto.setId(VALID_ID);
        userDto.setLastName("Kowalski");
        userDto.setFirstName("Patryk");
        userDto.setPassword("strongpassword");
        userDto.setRoleName(RoleName.CUSTOMER);
        userDto.setEmail(VALID_EMAIL);
        return userDto;
    }

    private UserUpdateDto getUserUpdate() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName("Przemek");
        userUpdateDto.setLastName("Kowalski");
        return userUpdateDto;
    }
}
