package com.pm.lovable_clone.service.impl;

import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private final UserRepository userRepository;

    @Override
    public void handleCheckoutSessionCompleted(UUID userId, String planTier) {
        log.info("Processing successful payment for user: {}, plan: {}", userId, planTier);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for webhook: " + userId));

        // A verified Stripe payment means we upgrade the user in our database.
        user.setSubscriptionTier(planTier);
        userRepository.save(user);

        log.info("🎉 User {} ({}) successfully upgraded to {} plan!", user.getName(), user.getEmail(), planTier);
    }

    @Override
    public void handleSubscriptionCancelled(UUID userId) {
        log.info("Processing subscription cancellation for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for webhook: " + userId));

        // If the subscription is cancelled, we revert the account back to the FREE tier.
        user.setSubscriptionTier("FREE");
        userRepository.save(user);

        log.info("User {} ({}) reverted to FREE plan", user.getName(), user.getEmail());
    }
}
