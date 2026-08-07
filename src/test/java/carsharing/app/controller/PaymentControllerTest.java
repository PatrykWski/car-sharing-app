package carsharing.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.rental.CreatePaymentRequestDto;
import carsharing.app.model.PaymentType;
import carsharing.app.model.StatusName;
import carsharing.app.security.JwtUtil;
import carsharing.app.security.SecurityConfig;
import carsharing.app.service.CustomUserDetailsService;
import carsharing.app.service.interfaces.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
public class PaymentControllerTest {

    private static final String VALID_EMAIL = "patrykk@gmail.com";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = null;
    private static final String VALID_SESSION_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void createStripeSession_ValidRequestDto_ReturnsStatusCreated() throws Exception {
        //given
        CreatePaymentRequestDto createPaymentRequestDto = new CreatePaymentRequestDto(VALID_ID);
        PaymentDto expected = getPaymentDto();

        when(paymentService.createStripeSession(VALID_EMAIL, createPaymentRequestDto))
                .thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequestDto)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        PaymentDto actual = objectMapper.readValue(json, PaymentDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void createStripeSession_InvalidRequest_ReturnsStatusBadRequest() throws Exception {
        //given
        CreatePaymentRequestDto createPaymentRequestDto = new CreatePaymentRequestDto(INVALID_ID);

        //when & then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getPayments_WithId_ReturnsStatusOk() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentService.getPayments(VALID_EMAIL, VALID_ID, pageable))
                .thenReturn(Page.empty());

        //when & then
        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = "MANAGER")
    void getPayments_WithoutId_ReturnsStatusOk() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentService.getPayments(VALID_EMAIL, null, pageable))
                .thenReturn(Page.empty());

        //when & then
        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void verifyPaymentSuccess_ValidSessionId_ReturnsStatusOk() throws Exception {
        //given
        PaymentDto expected = getPaymentDto();
        when(paymentService.verifyPaymentSuccess(VALID_SESSION_ID))
                .thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(get("/payments/success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", VALID_SESSION_ID))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        PaymentDto actual = objectMapper.readValue(json, PaymentDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void verifyPaymentSuccess_InvalidSessionId_ReturnsBadRequest() throws Exception {
        //given & when & then
        mockMvc.perform(get("/payments/success")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sessionId", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelPayment_ValidSessionId_ReturnsStatusOk() throws Exception {
        //given
        PaymentDto expected = getPaymentDto();
        when(paymentService.cancelPayment(VALID_SESSION_ID))
                .thenReturn(expected);

        //when & then
        MvcResult result = mockMvc.perform(get("/payments/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .param("sessionId", VALID_SESSION_ID))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PaymentDto actual = objectMapper.readValue(json, PaymentDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void cancelPayment_InvalidSessionId_ReturnsBadRequest() throws Exception {
        //given & when & then
        mockMvc.perform(get("/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", ""))
                .andExpect(status().isBadRequest());
    }

    private PaymentDto getPaymentDto() throws MalformedURLException {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(VALID_ID);
        paymentDto.setPaymentType(PaymentType.PAYMENT);
        paymentDto.setUrl(new URL("https://example.com"));
        paymentDto.setSessionId(VALID_SESSION_ID);
        paymentDto.setRentalId(VALID_ID);
        paymentDto.setStatusName(StatusName.PENDING);
        paymentDto.setAmountToPay(new BigDecimal(80));
        return paymentDto;
    }
}
