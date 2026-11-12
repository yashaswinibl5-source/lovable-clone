package com.pm.lovable_clone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Plan tier is required (e.g., PRO)")
    private String planTier = "PRO";
}
