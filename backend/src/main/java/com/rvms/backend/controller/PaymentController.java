package com.rvms.backend.controller;

import com.rvms.backend.dto.payment.PaymentRequest;
import com.rvms.backend.dto.payment.PaymentResponse;
import com.rvms.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Mock payment processing and refunds")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/rental/{rentalId}")
    public List<PaymentResponse> getByRental(@PathVariable Long rentalId) {
        return paymentService.getByRental(rentalId);
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> process(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable Long id) {
        return paymentService.refund(id);
    }
}
