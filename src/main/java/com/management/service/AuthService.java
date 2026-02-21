package com.management.service;

import com.management.dto.request.LoginRequest;
import com.management.dto.request.RegisterRequest;
import com.management.dto.response.AuthResponse;
import com.management.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}