package com.pm.lovable_clone.controller;

import com.pm.lovable_clone.dto.CheckoutRequest;
import com.pm.lovable_clone.dto.CheckoutResponse;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/checkout-session")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(@Valid @RequestBody CheckoutRequest request) {
        // The authenticated user is pulled from the JWT stored in Spring Security.
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        CheckoutResponse response = paymentService.createCheckoutSession(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    // Stripe redirects the browser here after a successful hosted checkout.
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok("Payment successful! Stripe Checkout Session ID: " + sessionId);
    }

    // Stripe redirects here if the user cancels checkout.
    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("Payment was cancelled by the user.");
    }
}
