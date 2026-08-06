package carsharing.app.controller;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.security.JwtAuthenticationFilter;
import carsharing.app.security.JwtUtil;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = RentalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
public class RentalControllerTest {
    private static final Long VALID_ID = 1L;
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
    void testSecurity() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println(auth);
    }


    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {"MANAGER", "CUSTOMER"})
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

    private RentalRequestDto getRentalRequest() {
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setUserId(VALID_ID);
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
