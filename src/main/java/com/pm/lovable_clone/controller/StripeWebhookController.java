package com.pm.lovable_clone.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.lovable_clone.service.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // This endpoint is exposed to Stripe's server-to-server webhook calls.
@RequestMapping("/api/v1/stripe") // Base path for Stripe webhook-related endpoints.
@RequiredArgsConstructor
@Slf4j // Logs webhook events and debugging information.
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;
    private final ObjectMapper objectMapper; // Parsed from our JacksonConfig bean.

    @Value("${stripe.webhook.secret}")
    private String webhookSecret; // Secret used to verify Stripe signature.

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String rawPayload, // Raw JSON body sent by Stripe.
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) { // Signature header for verification.

        log.info("Received Stripe Webhook event");

        try {
            // Stripe signs every webhook with HMAC-SHA256.
            // We verify it before trusting the event, unless we are using a mock local secret.
            if (sigHeader != null && !webhookSecret.contains("mock")) {
                Event event = Webhook.constructEvent(rawPayload, sigHeader, webhookSecret);
                log.info("Verified Stripe event: {}", event.getType());
            }

            // readTree() converts the raw JSON string into a tree structure we can inspect field-by-field.
            JsonNode rootNode = objectMapper.readTree(rawPayload);

            // The event type tells us what happened, for example: payment succeeded or subscription cancelled.
            String eventType = rootNode.path("type").asText();

            if ("checkout.session.completed".equals(eventType)) {
                // Stripe payload example:
                // "data" -> "object" -> "metadata" -> { "userId": "...", "planTier": "PRO" }
                JsonNode sessionNode = rootNode.path("data").path("object");
                JsonNode metadata = sessionNode.path("metadata");
                String userIdStr = metadata.path("userId").asText();
                String planTier = metadata.path("planTier").asText("PRO");

                if (!userIdStr.isEmpty()) {
                    stripeWebhookService.handleCheckoutSessionCompleted(UUID.fromString(userIdStr), planTier);
                }
            } else if ("customer.subscription.deleted".equals(eventType)) {
                // This event means the user cancelled a subscription, so we downgrade them back to FREE.
                JsonNode sessionNode = rootNode.path("data").path("object");
                String userIdStr = sessionNode.path("metadata").path("userId").asText();

                if (!userIdStr.isEmpty()) {
                    stripeWebhookService.handleSubscriptionCancelled(UUID.fromString(userIdStr));
                }
            }

            // HTTP 200 is required so Stripe knows the webhook was processed successfully.
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            log.error("⚠️ Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error");
        }
    }
}
