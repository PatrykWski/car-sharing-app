package carsharing.app.mapper;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
}
