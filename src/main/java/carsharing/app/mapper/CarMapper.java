package carsharing.app.mapper;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CarRequest;
import carsharing.app.dto.car.UpdateCarRequest;
import carsharing.app.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CarRequest carRequest);

    void updateACar(@MappingTarget Car car, UpdateCarRequest updateCarRequest);
}
