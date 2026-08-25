package carsharing.app.service.interfaces;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalDto addNewRental(String email, RentalRequestDto requestDto);

    Page<RentalDto> getAllActualRentalsByUserId(Long userId, String email,
                                                boolean isActive, Pageable pageable);

    RentalDto getSpecificRentalById(Long id, String email);

    RentalDto setActualReturnDate(Long id);

}
