package com.pm.lovable_clone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Tells Spring this class contains bean definitions for the application context.
public class JacksonConfig {

    // ObjectMapper is the JSON parser used to read Stripe webhook payloads.
    // Without this bean, Spring cannot inject ObjectMapper into controllers/services.
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // JavaTimeModule lets Jackson correctly handle LocalDateTime and other Java time types.
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}
