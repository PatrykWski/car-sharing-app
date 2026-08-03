package carsharing.app.service.interfaces;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarDto addNewCar(CarRequest carRequest);

    Page<CarDto> getPageOfCars(Pageable pageable);

    CarDto getCarById(Long id);

    CarDto updateCarById(Long id, UpdateCarRequest updateCarRequest);

    void deleteCarById(Long id);
}
