package com.pm.lovable_clone.service.impl;

import com.pm.lovable_clone.dto.CheckoutRequest;
import com.pm.lovable_clone.dto.CheckoutResponse;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.pro-monthly-price-in-cents:2000}")
    private Long proMonthlyPriceInCents;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        // Stripe SDK needs a secret key before any payment request can be created.
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public CheckoutResponse createCheckoutSession(UUID userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            // Stripe hosted checkout keeps card details off our backend and on Stripe's secure page.
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(user.getEmail())
                    .putMetadata("userId", user.getId().toString())
                    .putMetadata("planTier", request.getPlanTier())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(proMonthlyPriceInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Lovable Clone PRO Plan")
                                                                    .setDescription("Unlimited AI App Generation, Custom Domains, and Team Workspaces")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // This request creates a secure Stripe Checkout session and returns a URL the browser can redirect to.
            Session session = Session.create(params);

            return CheckoutResponse.builder()
                    .checkoutUrl(session.getUrl())
                    .sessionId(session.getId())
                    .build();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe payment session creation failed: " + e.getMessage(), e);
        }
    }
}
