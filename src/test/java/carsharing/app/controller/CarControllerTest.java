package carsharing.app.controller;

import carsharing.app.security.SecurityConfig;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.context.annotation.Import;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.model.TypeName;
import carsharing.app.security.JwtUtil;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.CarService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CarController.class)
@Import(SecurityConfig.class)
public class CarControllerTest {

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void addNewCar_ValidCarRequest_ReturnsStatusCreated() throws Exception {
        //given
        CarRequest carRequest = getCarRequest();
        CarDto expected = getCarDto();
        when(carService.addNewCar(carRequest)).thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        CarDto actual = objectMapper.readValue(json, CarDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void addNewCar_ValidCarRequestButWrongRole_ReturnsStatusForbidden() throws Exception {
        //given
        CarRequest carRequest = getCarRequest();

        //when & then
        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void addNewCar_InvalidCarRequest_ReturnsBadRequest() throws Exception {
        //given
        CarRequest carRequest = new CarRequest();

        //when & then
        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER"})
    void getPageOfCars_CarsExist_ReturnsStatusOk() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("brand"));
        CarDto carDto = getCarDto();
        Page<CarDto> expected = new PageImpl<>(List.of(carDto), pageable, 1);
        when(carService.getPageOfCars(pageable)).thenReturn(expected);

        //when & then
        mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getPageOfCars_CarsDoesNotExist_ReturnsStatusOk() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("brand"));
        Page<CarDto> expected = new PageImpl<>(List.of(), pageable, 1);
        when(carService.getPageOfCars(pageable)).thenReturn(expected);

        //when & then
        mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER"})
    void getCarById_ValidId_ReturnsStatusOk() throws Exception {
        //given
        CarDto expected = getCarDto();
        when(carService.getCarById(VALID_ID)).thenReturn(expected);

        //when & then
        mockMvc.perform(get("/cars/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getCarById_InvalidId_ReturnsNotFound() throws Exception {
        //given
        when(carService.getCarById(INVALID_ID))
                .thenThrow(new EntityNotFoundException("Car not found"));

        //when & then
        mockMvc.perform(get("/cars/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void updateCarById_ValidRequest_ReturnsStatusOk() throws Exception {
        //given
        UpdateCarRequest updateCarRequest = new UpdateCarRequest();
        updateCarRequest.setDailyFee(new BigDecimal(30));
        updateCarRequest.setInventory(10);
        CarDto expected = getCarDto();
        when(carService.updateCarById(VALID_ID, updateCarRequest)).thenReturn(expected);

        //when & then
        mockMvc.perform(put("/cars/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()));
    }
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void updateCarById_ValidRequestButBadRole_ReturnsForbidden() throws Exception {
        //given
        UpdateCarRequest updateCarRequest = new UpdateCarRequest();
        updateCarRequest.setDailyFee(new BigDecimal(30));
        updateCarRequest.setInventory(10);

        //when & then
        mockMvc.perform(put("/cars/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCarRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void updateCarById_InvalidRequest_ReturnsBadRequest() throws Exception {
        //given
        UpdateCarRequest updateCarRequest = new UpdateCarRequest();
        updateCarRequest.setInventory(-5);
        updateCarRequest.setDailyFee(new BigDecimal(-20));

        //when & then
        mockMvc.perform(put("/cars/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCarRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void updateCarById_InvalidId_ReturnsNotFound() throws Exception {
        //given
        UpdateCarRequest updateCarRequest = new UpdateCarRequest();
        updateCarRequest.setDailyFee(new BigDecimal(10));
        updateCarRequest.setInventory(10);
        when(carService.updateCarById(INVALID_ID, updateCarRequest))
                .thenThrow(new EntityNotFoundException("Car doesn't exist"));

        //when & then
        mockMvc.perform(put("/cars/{id}", INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCarRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void deleteCar_ValidId_ReturnsNoContent() throws Exception {
        //given
        doNothing().when(carService).deleteCarById(VALID_ID);

        //when & then
        mockMvc.perform(delete("/cars/{id}", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void deleteCar_ValidIdButBadRole_ReturnsForbidden() throws Exception {
        //given & when & then
        mockMvc.perform(delete("/cars/{id}", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void deleteCar_InvalidId_ReturnsNotFound() throws Exception {
        //given
        doThrow(new EntityNotFoundException("Car not found")).when(carService)
                .deleteCarById(INVALID_ID);

        //when & then
        mockMvc.perform(delete("/cars/{id}", INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private CarRequest getCarRequest() {
        CarRequest carRequest = new CarRequest();
        carRequest.setBrand("Seat");
        carRequest.setInventory(10);
        carRequest.setModel("Ibiza");
        carRequest.setTypeName(TypeName.HATCHBACK);
        carRequest.setDailyFee(new BigDecimal(20));
        return carRequest;
    }

    private CarDto getCarDto() {
        CarDto carDto = new CarDto();
        carDto.setInventory(20);
        carDto.setId(1L);
        carDto.setBrand("Seat");
        carDto.setModel("Ibiza");
        carDto.setDailyFee(new BigDecimal(20));
        carDto.setTypeName(TypeName.HATCHBACK);
        return carDto;
    }
}
