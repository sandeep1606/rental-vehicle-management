package com.rvms.backend.mapper;

import com.rvms.backend.dto.payment.PaymentResponse;
import com.rvms.backend.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getRental().getId(),
                p.getAmount(),
                p.getStatus().name(),
                p.getMethod(),
                p.getTransactionRef(),
                p.getPaidAt()
        );
    }
}
