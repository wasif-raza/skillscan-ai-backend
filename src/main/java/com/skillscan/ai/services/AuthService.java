package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    String login(LoginRequest request);
}
