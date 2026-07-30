package com.bank.auth.service;

import com.bank.auth.dto.request.LoginRequest;
import com.bank.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String accessToken);

    LoginResponse refresh(String refreshToken);

}
