package carsharing.app.service.interfaces;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface PaymentService {
    PaymentDto createStripeSession(UserDetails userDetails, CreatePaymentRequestDto request);

    Page<PaymentDto> getPayments(UserDetails userDetails, Long userId, Pageable pageable);

    PaymentDto verifyPaymentSuccess(String sessionId);

    PaymentDto cancelPayment(String sessionId);
}
