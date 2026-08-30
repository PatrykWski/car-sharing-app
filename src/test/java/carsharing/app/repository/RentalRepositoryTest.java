package carsharing.app.repository;

import carsharing.app.model.Rental;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class RentalRepositoryTest {
    private static final Long USER_ID = 1L;

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Should return active rentals when isActive is true")
    void findRentals_ActiveRentals_ReturnsPage() {
        //given
        Rental activeRental = getActiveRental();
        rentalRepository.save(activeRental);

        Rental completedRental = getActiveRental();
        completedRental.setActualReturnDate(LocalDate.now().minusDays(1));
        rentalRepository.save(completedRental);

        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<Rental> result = rentalRepository.findRentals(USER_ID, true, pageable);

        //then
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(activeRental.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should return completed rentals when isActive is false")
    void findRentals_CompletedRentals_ReturnsPage() {
        //given
        Rental activeRental = getActiveRental();
        rentalRepository.save(activeRental);

        Rental completedRental = getActiveRental();
        completedRental.setActualReturnDate(LocalDate.now().minusDays(1));
        rentalRepository.save(completedRental);

        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<Rental> result = rentalRepository.findRentals(USER_ID, false, pageable);

        //then

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(completedRental.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should return all rentals when userId and isActive are null")
    void findRentals_AllRentalsNullParams_ReturnsPage() {
        //given
        Rental rental1 = getActiveRental();
        rental1.setUserId(1L);
        rentalRepository.save(rental1);

        Rental rental2 = getActiveRental();
        rental2.setUserId(2L);
        rental2.setActualReturnDate(LocalDate.now().minusDays(1));
        rentalRepository.save(rental2);

        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<Rental> result = rentalRepository.findRentals(null, null, pageable);

        //then
        Assertions.assertEquals(2, result.getTotalElements());
    }

    private Rental getActiveRental() {
        Rental activeRental = new Rental();
        activeRental.setUserId(USER_ID);
        activeRental.setCarId(10L);
        activeRental.setRentalDate(LocalDate.now().minusDays(3));
        activeRental.setReturnDate(LocalDate.now().plusDays(2));
        activeRental.setActualReturnDate(null);
        return activeRental;
    }
}
