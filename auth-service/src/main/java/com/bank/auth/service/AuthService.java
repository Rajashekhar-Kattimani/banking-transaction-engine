package com.bank.auth.service;

import com.bank.auth.dto.request.LoginRequest;
import com.bank.auth.dto.request.RegisterRequest;
import com.bank.auth.dto.response.LoginResponse;
import com.bank.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String accessToken);

    LoginResponse refresh(String refreshToken);
}