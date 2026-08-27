package carsharing.app.service;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import carsharing.app.dto.telegram.TelegramMessageRequest;
import carsharing.app.exception.AuthenticationException;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.NotificationError;
import carsharing.app.exception.RentalNotFinished;
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
import carsharing.app.service.interfaces.NotificationService;
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
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final List<PaymentMethod> paymentMethodsList;
    private Map<PaymentType, PaymentMethod> paymentMethods;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Value("${STRIPE_URL}")
    private String url;

    @Value("${TELEGRAM_CHAT_ID}")
    private String chatId;

    @PostConstruct
    public void initStripeAndMethods() {
        Stripe.apiKey = stripeSecretKey;
        this.paymentMethods = paymentMethodsList.stream()
                .collect(Collectors
                        .toMap(PaymentMethod::getSupportedType, paymentMethod -> paymentMethod));
    }

    @Override
    @Transactional
    public PaymentDto createStripeSession(
            String email,
            CreatePaymentRequestDto request) {
        Rental rental = getRental(request.rentalId());

        User user = getUser(rental.getUserId());

        if (email.equals(user.getEmail())) {

            BigDecimal amountToPay = amountToPay(rental, request.type());

            long amountInCents = amountToPay.multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams params = getParams(amountInCents, request.rentalId());

            try {
                Session session = Session.create(params);

                Payment payment = createPayment(
                        request.rentalId(), amountToPay, session.getUrl(), session.getId(),
                        request.type());

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
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPayments(String email, Long userId, Pageable pageable) {
        User loggedInUser = userRepository.findByEmail(email).orElseThrow(
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

        if (!user.getEmail().equals(email) && !isManager) {
            throw new AuthenticationException("Unauthorized user");
        }
        return getPaymentsByUserId(user.getId(), pageable);
    }

    @Override
    @Transactional
    public PaymentDto verifyPaymentSuccess(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) && "paid"
                    .equals(session.getPaymentStatus())) {

                Payment payment = getPayment(sessionId);
                payment.setStatusName(StatusName.PAID);
                Payment savedPayment = paymentRepository.save(payment);

                TransactionSynchronizationManager
                        .registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                String message = getSuccessfulMessage(savedPayment);

                                TelegramMessageRequest request = new TelegramMessageRequest(
                                        chatId,
                                        message);

                                sendNotification(request);
                            }
                        });

                return paymentMapper.toDto(payment);
            }
        } catch (StripeException e) {
            throw new StripeProcessingException("Error while retrieving Stripe session", e);
        }
        throw new StripeProcessingException("Payment status is not complete or its not paid yet");
    }

    @Override
    @Transactional
    public PaymentDto cancelPayment(String sessionId) {
        if (sessionId != null) {
            Payment payment = getPayment(sessionId);
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
                .setSuccessUrl(createUrl("success/"))
                .setCancelUrl(createUrl("cancel/"))
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
            Long rentalId, BigDecimal amountToPay,
            String url, String sessionId, PaymentType paymentType) {
        try {
            Payment payment = new Payment();
            payment.setRentalId(rentalId);
            payment.setAmountToPay(amountToPay);
            payment.setUrl(new URL(url));
            payment.setSessionId(sessionId);
            payment.setPaymentType(paymentType);
            payment.setStatusName(StatusName.PENDING);
            return payment;
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendNotification(TelegramMessageRequest telegramMessageRequest) {
        String result = notificationService.sendNotification(telegramMessageRequest);
        if (result == null || result.isEmpty()) {
            throw new NotificationError("Result from notificationService"
                    + " was null or empty");
        }
    }

    private BigDecimal amountToPay(Rental rental, PaymentType paymentType) {
        if (rental.getActualReturnDate() != null) {
            BigDecimal standard = paymentMethods.get(PaymentType.PAYMENT)
                    .totalToPay(rental.getId());
            if (!rental.getRentalDate().isAfter(rental.getActualReturnDate())) {
                return standard;
            }
            BigDecimal fee = paymentMethods.get(paymentType).totalToPay(rental.getId());
            return standard.add(fee);
        }
        throw new RentalNotFinished("Rental is not finished yet!");
    }

    private Rental getRental(Long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id: " + rentalId
                        + " doesn't exist"));
    }

    private String createUrl(String type) {
        return UriComponentsBuilder.fromUriString(url)
                .path(type)
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build()
                .toUriString();
    }

    private String getSuccessfulMessage(Payment payment) {
        return String.format(
                "Payment has been successfully paid! \nPaymentID: %d"
                        + "\nAmount: %s \nRentalID: %d",
                payment.getId(),
                payment.getAmountToPay(),
                payment.getRentalId());
    }
}
