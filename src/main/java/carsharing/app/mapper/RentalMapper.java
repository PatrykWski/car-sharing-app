package carsharing.app.mapper;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.model.Car;
import carsharing.app.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    @Mapping(target = "id", source = "car.id")
    @Mapping(target = "model", source = "car.model")
    @Mapping(target = "brand", source = "car.brand")
    @Mapping(target = "typeName", source = "car.typeName")
    @Mapping(target = "dailyFee", source = "car.dailyFee")
    RentalDto toDto(Rental rental, Car car);

    Rental toModel(RentalRequestDto requestDto);
}
