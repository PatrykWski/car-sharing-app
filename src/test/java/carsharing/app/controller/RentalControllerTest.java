package carsharing.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.security.JwtUtil;
import carsharing.app.security.SecurityConfig;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(RentalController.class)
@Import(SecurityConfig.class)
public class RentalControllerTest {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String VALID_EMAIL = "patrykw@gmail.com";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RentalService rentalService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void addNewRental_ValidRequest_ReturnIsCreated() throws Exception {
        //given
        RentalRequestDto request = getRentalRequest();
        RentalDto expected = getRentalDto();

        when(rentalService.addNewRental(VALID_EMAIL, request)).thenReturn(expected);

        //when
        MvcResult result = mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        //then
        String json = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(json, RentalDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void addNewRental_InvalidRequest_ReturnBadRequest() throws Exception {
        //given
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setReturnDate(null);
        requestDto.setRentalDate(null);
        requestDto.setCarId(-3L);

        //when & then
        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getAllActualRentalsByUserId_ValidParams_ReturnsOk() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        when(rentalService.getAllActualRentalsByUserId(VALID_ID, VALID_EMAIL, true, pageable))
                .thenReturn(Page.empty());

        //when & then
        mockMvc.perform(get("/rentals/{userId}/all", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isActive", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getAllActualRentalsByUserId_BadId_ReturnsNotFound() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        when(rentalService.getAllActualRentalsByUserId(INVALID_ID, VALID_EMAIL, false, pageable))
                .thenThrow(new EntityNotFoundException("User not found"));

        //when & then
        mockMvc.perform(get("/rentals/{userId}/all", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isActive", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getAllActualRentalsByUserId_InvalidParams_ReturnsBadRequest() throws Exception {
        //given & when & then
        mockMvc.perform(get("/rentals/{userId}/all", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isActive", "null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getRentalById_ValidId_ReturnsIsOk() throws Exception {
        //given
        RentalDto rentalDto = getRentalDto();

        when(rentalService.getSpecificRentalById(VALID_ID, VALID_EMAIL)).thenReturn(rentalDto);

        //when & then
        MvcResult result = mockMvc.perform(get("/rentals/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(json, RentalDto.class);

        Assertions.assertEquals(rentalDto, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getRentalById_InvalidId_ReturnsNotFound() throws Exception {
        //given
        when(rentalService.getSpecificRentalById(INVALID_ID, VALID_EMAIL))
                .thenThrow(new EntityNotFoundException("Rental not found"));

        //when & then
        mockMvc.perform(get("/rentals/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void getRentalById_ValidIdButCustomer_ReturnsIsOk() throws Exception {
        //given
        RentalDto rentalDto = getRentalDto();

        when(rentalService.getSpecificRentalById(VALID_ID, VALID_EMAIL)).thenReturn(rentalDto);

        //when & then
        MvcResult result = mockMvc.perform(get("/rentals/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(json, RentalDto.class);

        Assertions.assertEquals(rentalDto, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void setActualReturnDate_ValidId_ReturnsOk() throws Exception {
        //given
        RentalDto rentalDto = getRentalDto();
        when(rentalService.setActualReturnDate(VALID_ID)).thenReturn(rentalDto);

        // when & then
        MvcResult result = mockMvc.perform(put("/rentals/return/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(json, RentalDto.class);

        Assertions.assertEquals(rentalDto, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "CUSTOMER")
    void setActualReturnDate_ValidIdButBadRole_ReturnsForbidden() throws Exception {
        //given & when & then
        mockMvc.perform(put("/rentals/return/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void setActualReturnDate_InvalidId_ReturnsNotFound() throws Exception {
        //given
        when(rentalService.setActualReturnDate(INVALID_ID))
                .thenThrow(new EntityNotFoundException("User not found"));

        // when & then
        mockMvc.perform(put("/rentals/return/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private RentalRequestDto getRentalRequest() {
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(VALID_ID);
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(5));
        return requestDto;
    }

    private RentalDto getRentalDto() {
        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(VALID_ID);
        rentalDto.setCarId(VALID_ID);
        rentalDto.setUserId(VALID_ID);
        rentalDto.setRentalDate(LocalDate.now());
        rentalDto.setReturnDate(LocalDate.now().plusDays(5));
        rentalDto.setActualReturnDate(null);
        return rentalDto;
    }
}
