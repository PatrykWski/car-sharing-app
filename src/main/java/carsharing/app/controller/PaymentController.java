package carsharing.app.controller;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import carsharing.app.service.interfaces.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payment management", description = "Endpoints for payment management")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment session", description = "Create payment session")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public PaymentDto createStripeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreatePaymentRequestDto createPaymentRequestDto) {

        return paymentService.createStripeSession(userDetails.getUsername(),
                createPaymentRequestDto);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @Operation(summary = "Get payments", description = "Get payments by id or without")
    public Page<PaymentDto> getPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 10, page = 0)
            Pageable pageable) {

        return paymentService.getPayments(userDetails.getUsername(), userId, pageable);
    }

    @GetMapping("/success")
    public PaymentDto verifyPaymentSuccess(@RequestParam @NotBlank String sessionId) {
        return paymentService.verifyPaymentSuccess(sessionId);
    }

    @GetMapping("/cancel")
    public PaymentDto cancelPayment(@RequestParam @NotBlank String sessionId) {
        return paymentService.cancelPayment(sessionId);
    }
}
