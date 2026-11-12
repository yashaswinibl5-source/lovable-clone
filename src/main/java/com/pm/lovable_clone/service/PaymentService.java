package com.pm.lovable_clone.service;

import com.pm.lovable_clone.dto.CheckoutRequest;
import com.pm.lovable_clone.dto.CheckoutResponse;

import java.util.UUID;

public interface PaymentService {
    CheckoutResponse createCheckoutSession(UUID userId, CheckoutRequest request);
}
