package carsharing.app.service;

import static org.mockito.Mockito.when;

import carsharing.app.exception.EntityNotFoundException;
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
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    private static final Long VALID_ID = 1L;
    private static final String VALID_EMAIL = "patrykk@gmail.com";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ValidEmail_ReturnsUserDetails() {
        //given
        User user = getUser();
        UserDetails expected = user;
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));

        //when
        UserDetails actual = customUserDetailsService.loadUserByUsername(VALID_EMAIL);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loadUserByUsername_InvalidEmail_ThrowsEntityNotFoundException() {
        //given
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(VALID_EMAIL));
    }

    private User getUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setPassword("strongpassword");
        user.setRoleName(RoleName.CUSTOMER);
        user.setEmail(VALID_EMAIL);
        user.setFirstName("Patryk");
        user.setLastName("Kowalski");
        return user;
    }
}
