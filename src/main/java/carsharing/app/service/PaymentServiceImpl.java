package carsharing.app.service;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import carsharing.app.exception.AuthenticationException;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.StripeProcessingException;
import carsharing.app.mapper.PaymentMapper;
import carsharing.app.model.Payment;
import carsharing.app.model.PaymentType;
import carsharing.app.model.Rental;
import carsharing.app.model.RoleName;
import carsharing.app.model.StatusName;
import carsharing.app.model.User;
import carsharing.app.repository.PaymentRepository;
import carsharing.app.repository.RentalRepository;
import carsharing.app.repository.UserRepository;
import carsharing.app.service.interfaces.PaymentService;
import carsharing.app.service.payment.PaymentMethod;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentMethod paymentMethod;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    @Value("${stripe.sk}")
    private String stripeSecretKey;

    @Value("${stripe.url}")
    private String url;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public PaymentDto createStripeSession(
            UserDetails userDetails,
            CreatePaymentRequestDto request) {

        Rental rental = rentalRepository.findById(request.rentalId()).orElseThrow(
                () -> new EntityNotFoundException("Rental with id: " + request.rentalId()
                        + " doesn't exist"));

        User user = getUser(rental.getUserId());

        if (userDetails.getUsername().equals(user.getEmail())) {
            BigDecimal amountToPay = paymentMethod.totalToPay(request.rentalId());

            long amountInCents = amountToPay.multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams params = getParams(amountInCents, request.rentalId());

            try {
                Session session = Session.create(params);

                Payment payment = createPayment(
                        request.rentalId(), amountToPay, session.getUrl(), session.getId());

                paymentRepository.save(payment);

                return paymentMapper.toDto(payment);
            } catch (StripeException ex) {
                throw new StripeProcessingException("Error while creating Stripe checkout session",
                        ex);
            }
        }
        throw new AuthenticationException("Unauthorized user");
    }

    @Override
    public Page<PaymentDto> getPayments(UserDetails userDetails, Long userId, Pageable pageable) {
        User loggedInUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("User not found"));
        boolean isManager = loggedInUser.getRoleName().equals(RoleName.MANAGER);

        if (userId == null && isManager) {
            return paymentRepository.findAll(pageable)
                    .map(paymentMapper::toDto);
        }
        if (userId == null) {
            return getPaymentsByUserId(loggedInUser.getId(), pageable);
        }

        User user = getUser(userId);

        if (!user.getEmail().equals(userDetails.getUsername()) && !isManager) {
            throw new AuthenticationException("Unauthorized user");
        }
        return getPaymentsByUserId(user.getId(), pageable);
    }

    @Override
    public PaymentDto verifyPaymentSuccess(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) && "paid"
                    .equals(session.getPaymentStatus())) {

                Payment payment = getPayment(sessionId);
                payment.setStatusName(StatusName.PAID);
                paymentRepository.save(payment);

                return paymentMapper.toDto(payment);
            }
        } catch (StripeException e) {
            throw new StripeProcessingException("Error while retrieving Stripe session", e);
        }
        throw new StripeProcessingException("Payment status is not complete or its not paid yet");
    }

    @Override
    public PaymentDto cancelPayment(String sessionId) {
        if (sessionId != null) {
            Payment payment = getPayment(sessionId);
            payment.setStatusName(StatusName.CANCELED);
            paymentRepository.save(payment);
            return paymentMapper.toDto(payment);
        }
        throw new EntityNotFoundException("Payment with this session id doesn't exist");
    }

    private Payment getPayment(String sessionId) {
        return paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment with session id: " + sessionId
                        + " doesnt exist"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with rental id: "
                        + userId + " doesnt exist"));
    }

    private Page<PaymentDto> getPaymentsByUserId(Long userId, Pageable pageable) {
        List<Rental> rentals = rentalRepository.findRentalByUserId(userId);
        List<Long> rentalIds = rentals.stream()
                .map(Rental::getId)
                .toList();
        return paymentRepository.findAllByRentalIdIn(rentalIds, pageable)
                .map(paymentMapper::toDto);
    }

    private SessionCreateParams getParams(long amountInCents, Long rentalId) {
        return SessionCreateParams.builder()
                .setMode(Mode.PAYMENT)
                .setSuccessUrl(url + "/payments/success")
                .setCancelUrl(url + "/payments/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName("Car Rental #"
                                                                        + rentalId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private Payment createPayment(
            Long rentalId, BigDecimal amountToPay, String url, String sessionId) {
        try {
            Payment payment = new Payment();
            payment.setRentalId(rentalId);
            payment.setAmountToPay(amountToPay);
            payment.setUrl(new URL(url));
            payment.setSessionId(sessionId);
            payment.setPaymentType(PaymentType.PAYMENT);
            payment.setStatusName(StatusName.PENDING);
            return payment;
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
