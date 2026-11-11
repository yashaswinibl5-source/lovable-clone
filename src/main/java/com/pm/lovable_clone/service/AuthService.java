package com.pm.lovable_clone.service;

import com.pm.lovable_clone.dto.AuthResponse;
import com.pm.lovable_clone.dto.LoginRequest;
import com.pm.lovable_clone.dto.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}
