package com.pm.lovable_clone.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String checkoutUrl;
    private String sessionId;
}
