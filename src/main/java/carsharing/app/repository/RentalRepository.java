package carsharing.app.repository;

import carsharing.app.model.Rental;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("SELECT r FROM Rental r "
            + "WHERE r.userId = :id AND "
            + "((:isActive = true AND r.actualReturnDate IS NULL) "
            + "OR (:isActive = false AND r.actualReturnDate IS NOT NULL))")
    Page<Rental> findRentalByActualReturnDate(@Param("id") Long id,
                                              @Param("isActive") boolean isActive,
                                              Pageable pageable);

    List<Rental> findRentalByUserId(Long userId);
}
