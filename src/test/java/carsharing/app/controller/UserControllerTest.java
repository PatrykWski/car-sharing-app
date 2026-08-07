package carsharing.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserUpdateDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.model.RoleName;
import carsharing.app.security.JwtUtil;
import carsharing.app.security.SecurityConfig;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    private static final String VALID_EMAIL = "patrykk@gmail.com";
    private static final String INVALID_EMAIL = "patryk12";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void updateUserRole_ValidIdAndRole_ReturnsStatusOk() throws Exception {
        //given
        RoleName roleName = RoleName.CUSTOMER;
        UserDto expected = getUserDto();

        when(userService.updateUserRole(VALID_ID, roleName)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(put("/users/{id}/role", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("roleName", "CUSTOMER"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        UserDto actual = objectMapper.readValue(json, UserDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void updateUserRole_InvalidId_ReturnsNotFound() throws Exception {
        //given
        when(userService.updateUserRole(INVALID_ID, RoleName.CUSTOMER))
                .thenThrow(new EntityNotFoundException("User not found"));

        //when & then
        mockMvc.perform(put("/users/{id}/role", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("roleName", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void updateUserRole_InvalidParamRole_ReturnsBadRequest() throws Exception {
        //given & when & then
        mockMvc.perform(put("/users/{id}/role", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("roleName", "ANONYMOUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void updateUserRole_InvalidUserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/users/{id}/role", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("roleName", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void getUser_UserExist_ReturnsStatusOk() throws Exception {
        //given
        UserDto expected = getUserDto();
        when(userService.getUser(VALID_EMAIL)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        UserDto actual = objectMapper.readValue(json, UserDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = INVALID_EMAIL, roles = "CUSTOMER")
    void getUser_WrongEmail_ReturnsNotFound() throws Exception {
        //given
        when(userService.getUser(INVALID_EMAIL))
                .thenThrow(new EntityNotFoundException("User not found"));

        //when & then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void updateProfile_ValidRequest_ReturnsStatusOk() throws Exception {
        //given
        UserUpdateDto userUpdateDto = getUserUpdate();
        UserDto expected = getUserDto();
        when(userService.updateProfile(VALID_EMAIL, userUpdateDto)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        UserDto actual = objectMapper.readValue(json, UserDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void updateProfile_InvalidRequest_ReturnsBadRequest() throws Exception {
        //given
        UserUpdateDto userUpdateDto = new UserUpdateDto();

        //when & then
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = INVALID_EMAIL, roles = "CUSTOMER")
    void updateProfile_InvalidEmail_ReturnsNotFound() throws Exception {
        //given
        UserUpdateDto userUpdateDto = getUserUpdate();

        when(userService.updateProfile(INVALID_EMAIL, userUpdateDto))
                .thenThrow(new EntityNotFoundException("User not found"));

        //when & then
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound());
    }

    private UserDto getUserDto() {
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
