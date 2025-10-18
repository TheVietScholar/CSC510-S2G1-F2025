package com.boozebuddies.mapper;

import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.dto.PaymentRequest;
import com.boozebuddies.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDTO(Payment payment) {
        if (payment == null) return null;
        
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .failureReason(payment.getFailureReason())
                .build();
    }

    public Payment toEntity(PaymentRequest request) {
        if (request == null) return null;
        
        return Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();
    }
}