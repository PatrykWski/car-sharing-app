package carsharing.app.mapper;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import carsharing.app.model.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    RentalDto toDto(Rental rental);

    Rental toModel(RentalRequestDto requestDto);
}
