package com.pm.lovable_clone.service.impl;

import com.pm.lovable_clone.dto.AuthResponse;
import com.pm.lovable_clone.dto.LoginRequest;
import com.pm.lovable_clone.dto.SignupRequest;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.BadRequestException;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.AuthService;
import com.pm.lovable_clone.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse signup(SignupRequest request) {
        // 1. Reject duplicate accounts before saving anything.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        // 2. Hash the password using BCrypt before saving into the database.
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .subscriptionTier("FREE")
                .build();

        User savedUser = userRepository.save(user);

        // 3. Issue a JWT immediately after successful signup.
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .subscriptionTier(savedUser.getSubscriptionTier())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. Load the user from the database using their email.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // 2. Verify the raw password against the stored BCrypt hash.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // 3. If valid, issue a JWT for future authenticated requests.
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .subscriptionTier(user.getSubscriptionTier())
                .build();
    }
}
