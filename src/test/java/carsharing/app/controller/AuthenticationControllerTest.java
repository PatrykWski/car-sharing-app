package carsharing.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.dto.auth.LoginDto;
import carsharing.app.dto.auth.LoginRequestDto;
import carsharing.app.dto.register.UserRequestDto;
import carsharing.app.dto.register.UserResponseDto;
import carsharing.app.model.RoleName;
import carsharing.app.security.AuthenticationService;
import carsharing.app.security.JwtUtil;
import carsharing.app.security.SecurityConfig;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
public class AuthenticationControllerTest {
    private static final String VALID_EMAIL = "patrykk@gmail.com";
    private static final String VALID_PASSWORD = "strongPassword";
    private static final String INVALID_EMAIL = "patryk12";
    private static final String INVALID_PASSWORD = "1234";
    private static final Long VALID_ID = 1L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_ValidUserRequestDto_ReturnsStatusCreated() throws Exception {
        //given
        UserRequestDto userRequestDto = getUserRequestDto();
        UserResponseDto expected = getUserResponseDto();
        when(registrationService.register(userRequestDto)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(json, UserResponseDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void register_InvalidUserRequestDto_ReturnsStatusBadRequest() throws Exception {
        //given
        UserRequestDto userRequestDto = new UserRequestDto();

        //when & then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidLoginRequestDto_ReturnsLoginDto() throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto(VALID_EMAIL, VALID_PASSWORD);
        LoginDto expected = new LoginDto("abcd");
        when(authenticationService.authenticate(loginRequestDto)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        LoginDto actual = objectMapper.readValue(json, LoginDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void login_InvalidLoginRequestDto_ReturnsBadRequest() throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto(INVALID_EMAIL, INVALID_PASSWORD);

        //when & then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    private UserRequestDto getUserRequestDto() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(VALID_EMAIL);
        userRequestDto.setFirstName("Patryk");
        userRequestDto.setLastName("Kowalski");
        userRequestDto.setPassword("strongPassword");
        userRequestDto.setRepeatPassword("strongPassword");
        return userRequestDto;
    }

    private UserResponseDto getUserResponseDto() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(VALID_EMAIL);
        userResponseDto.setId(VALID_ID);
        userResponseDto.setPassword("strongPassword");
        userResponseDto.setRoleName(RoleName.CUSTOMER);
        userResponseDto.setLastName("Kowalski");
        userResponseDto.setFirstName("Patryk");
        return userResponseDto;
    }
}
