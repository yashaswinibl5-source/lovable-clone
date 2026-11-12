package com.pm.lovable_clone.controller;

import com.pm.lovable_clone.dto.AuthResponse;
import com.pm.lovable_clone.dto.LoginRequest;
import com.pm.lovable_clone.dto.SignupRequest;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this class as a web controller that returns JSON responses.
@RequestMapping("/api/v1/auth") // Base path for all auth endpoints.
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows browser clients to call these APIs from different origins during local development.
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return ResponseEntity.ok(AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .subscriptionTier(user.getSubscriptionTier())
                .build());
    }
}
