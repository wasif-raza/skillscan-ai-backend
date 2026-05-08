package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.dto.response.AuthResponse;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
