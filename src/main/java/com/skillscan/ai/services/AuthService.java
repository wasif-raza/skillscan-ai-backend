package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RefreshRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.dto.response.AuthResponse;
import com.skillscan.ai.dto.response.RefreshResponse;

import java.util.Map;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String accessToken, String refreshToken);


    RefreshResponse refresh(RefreshRequest request);
}
