package carsharing.app.repository;

import carsharing.app.model.Payment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByRentalIdIn(Collection<Long> rentalIds, Pageable pageable);

    List<Payment> findAllByRentalIdIn(Collection<Long> rentalIds);

    Optional<Payment> findBySessionId(String sessionId);
}
