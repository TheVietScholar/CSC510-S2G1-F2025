package com.boozebuddies.mapper;

import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.dto.PaymentRequest;
import com.boozebuddies.entity.Payment;
import org.springframework.stereotype.Component;

/** Mapper for converting between Payment entities and Payment-related DTO objects. */
@Component
public class PaymentMapper {

  /**
   * Converts a Payment entity to a PaymentDTO.
   *
   * @param payment the payment entity to convert
   * @return the PaymentDTO, or null if the input is null
   */
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

  /**
   * Converts a PaymentRequest to a Payment entity.
   *
   * @param request the PaymentRequest to convert
   * @return the Payment entity, or null if the input is null
   */
  public Payment toEntity(PaymentRequest request) {
    if (request == null) return null;

    return Payment.builder()
        .amount(request.getAmount())
        .paymentMethod(request.getPaymentMethod())
        .build();
  }
}
