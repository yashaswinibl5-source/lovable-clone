package com.pm.lovable_clone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Enables the Spring Security filter chain for web requests.
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, @Secured checks on controller/service methods.
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT filter runs before the default username/password auth filter
    // so incoming requests can be authenticated using the Authorization: Bearer <token> header.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is a one-way password hashing algorithm used for secure storage.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // REST APIs are stateless, so CSRF protection is disabled for simplicity.
            .csrf(AbstractHttpConfigurer::disable)
            // JWT-based authentication means we do not maintain HTTP sessions.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints are public so users can sign up or log in.
                .requestMatchers("/api/v1/auth/**").permitAll()
                // SSE generator endpoint is public for the live preview flow.
                .requestMatchers("/api/v1/generator/**").permitAll()
                // Any other endpoint requires a valid JWT in the Authorization header.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

//Very simple mental model:
//
//1. User signs up/logs in
//- `AuthController` receives `POST /api/v1/auth/login` or `/signup`
//- `AuthServiceImpl` checks DB user
//- Password is verified with `BCryptPasswordEncoder.matches()`
//- If valid, `JwtService.generateToken(userId, email)` creates a signed JWT
//
//2. JWT is sent to client
//- Response includes:
//  `Authorization: Bearer <token>`
//- Client stores that token and sends it on every protected API call
//
//3. Every protected request passes through `JwtAuthenticationFilter`
//- Spring calls this filter before controller logic
//- It reads the `Authorization` header
//- If it starts with `Bearer `, it extracts the JWT
//- `jwtService.isTokenValid(jwt)` checks:
//  - signature is valid
//  - token is not expired
//- If valid, Spring creates an authentication object and stores it in `SecurityContextHolder`
//
//4. Security rules decide what is allowed
//- `SecurityConfig` says:
//  - `/api/v1/auth/**` → public
//  - `/api/v1/generator/**` → public
//  - everything else → authenticated
//
//5. Controller logic uses authenticated user
//- `SecurityContextHolder.getContext().getAuthentication().getName()`
//- That gives the logged-in email
//- You can load the user from DB and create projects for that user
//
//So the flow is:
//Login → JWT issued → Request includes JWT → Filter validates JWT → SecurityContext set → API allows request.