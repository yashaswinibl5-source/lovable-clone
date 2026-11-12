package com.pm.lovable_clone.service;

import java.util.UUID;

public interface StripeWebhookService {
    void handleCheckoutSessionCompleted(UUID userId, String planTier);
    void handleSubscriptionCancelled(UUID userId);
}
