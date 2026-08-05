package carsharing.app.service.interfaces;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentDto createStripeSession(String email, CreatePaymentRequestDto request);

    Page<PaymentDto> getPayments(String email, Long userId, Pageable pageable);

    PaymentDto verifyPaymentSuccess(String sessionId);

    PaymentDto cancelPayment(String sessionId);

}
