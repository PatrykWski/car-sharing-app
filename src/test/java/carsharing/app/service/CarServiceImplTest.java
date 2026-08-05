package carsharing.app.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.mapper.CarMapper;
import carsharing.app.model.Car;
import carsharing.app.model.TypeName;
import carsharing.app.repository.CarRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    void addNewCar_ValidCarRequest_ReturnsCarDto() {
        //given
        CarRequest carRequest = getCarRequest();
        Car car = getCar();
        CarDto expected = getCarDto();

        when(carMapper.toModel(carRequest)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        //when
        CarDto actual = carService.addNewCar(carRequest);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getPageOfCarS_CarsExist_ReturnsPageOfCarDto() {
        //given
        Car car = getCar();
        CarDto carDto = getCarDto();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> page = new PageImpl<>(List.of(car));
        Page<CarDto> expected = new PageImpl<>(List.of(carDto));
        when(carRepository.findAll(pageable)).thenReturn(page);
        when(carMapper.toDto(car)).thenReturn(carDto);

        //when
        Page<CarDto> actual = carService.getPageOfCars(pageable);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getPageOfCarS_CarsDoesNotExist_ReturnsEmptyPage() {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> page = new PageImpl<>(List.of());
        Page<CarDto> expected = new PageImpl<>(List.of());
        when(carRepository.findAll(pageable)).thenReturn(page);

        //when
        Page<CarDto> actual = carService.getPageOfCars(pageable);

        //then
        Assertions.assertEquals(expected.getSize() == 0, actual.getSize() == 0);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getCarById_ValidId_ReturnsCarDto() {
        //given
        Car car = getCar();
        CarDto expected = getCarDto();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);

        //when
        CarDto actual = carService.getCarById(VALID_ID);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getCatById_InvalidId_ReturnsEntityNotFoundException() {
        //given
        when(carRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> carService.getCarById(INVALID_ID));
    }

    @Test
    void updateCarById_ValidCarRequest_ReturnsCarDto() {
        //given
        Car car = getCar();
        UpdateCarRequest carRequest = getUpdateCarRequest();
        CarDto expected = getCarDto();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateACar(car, carRequest);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        //when
        CarDto actual = carService.updateCarById(VALID_ID, carRequest);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void updateCarById_InvalidId_ReturnsEntityNotFoundException() {
        //given
        when(carRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> carService.updateCarById(INVALID_ID, new UpdateCarRequest()));
    }

    @Test
    void deleteCarById_ValidId_ReturnsNothing() {
        //given
        Car car = getCar();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        doNothing().when(carRepository).delete(car);

        //when

        carService.deleteCarById(VALID_ID);
        //then

        verify(carRepository, times(1)).delete(car);
    }

    @Test
    void deleteCarById_InvalidId_ReturnsEntityNotFoundException() {
        //given
        when(carRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> carService.deleteCarById(INVALID_ID));
    }

    private Car getCar() {
        Car car = new Car();
        car.setInventory(20);
        car.setId(1L);
        car.setBrand("Seat");
        car.setModel("Ibiza");
        car.setDailyFee(new BigDecimal(20));
        car.setTypeName(TypeName.HATCHBACK);
        return car;
    }

    private CarRequest getCarRequest() {
        CarRequest carRequest = new CarRequest();
        carRequest.setBrand("Seat");
        carRequest.setInventory(10);
        carRequest.setModel("Ibiza");
        carRequest.setTypeName(TypeName.HATCHBACK);
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

    private UpdateCarRequest getUpdateCarRequest() {
        UpdateCarRequest updateCarRequest = new UpdateCarRequest();
        updateCarRequest.setDailyFee(new BigDecimal(20));
        updateCarRequest.setInventory(10);
        return updateCarRequest;
    }
}
