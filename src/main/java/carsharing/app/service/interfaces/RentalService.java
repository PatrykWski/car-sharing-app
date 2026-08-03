package carsharing.app.service.interfaces;

import carsharing.app.dto.rental.RentalDto;
import carsharing.app.dto.rental.RentalRequestDto;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface RentalService {
    RentalDto addNewRental(UserDetails userDetails, RentalRequestDto requestDto);

    Page<RentalDto> getAllRentalsByUserId(Long userId, UserDetails userDetails, Pageable pageable);

    RentalDto getSpecificRentalById(Long id);

    RentalDto setActualReturnDate(Long id);
}
