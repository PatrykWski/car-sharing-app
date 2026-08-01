package carsharing.app.service;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.mapper.CarMapper;
import carsharing.app.model.Car;
import carsharing.app.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarDto addNewCar(CarRequest carRequest) {
        Car car = carMapper.toModel(carRequest);
        Car savedCar = carRepository.save(car);
        return carMapper.toDto(savedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDto> getPageOfCars(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CarDto getCarById(Long id) {
        Car car = findCarById(id);
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarDto updateCarById(Long id, UpdateCarRequest updateCarRequest) {
        Car car = findCarById(id);
        carMapper.updateACar(car, updateCarRequest);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    @Transactional
    public void deleteCarById(Long id) {
        Car car = findCarById(id);
        carRepository.delete(car);
    }

    private Car findCarById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car with id: " + id + " doesn't exist"));
    }
}
