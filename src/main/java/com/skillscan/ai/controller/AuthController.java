package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.LogoutRequest;
import com.skillscan.ai.dto.request.RefreshRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.dto.response.AuthResponse;
import com.skillscan.ai.dto.response.RefreshResponse;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.AuthService;
import com.skillscan.ai.services.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository repo;
    private final JwtTokenProvider jwt;
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    //  REGISTER
    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Map.of("message", "User registered successfully");
    }
    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
    //  REFRESH
    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return authService.refresh(request);
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
           @Valid @RequestBody LogoutRequest logoutRequest
    ) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token found");
        }

        String accessToken = authHeader.substring(7);

        authService.logout(
                accessToken,
                logoutRequest.getRefreshToken()
        );

        return ResponseEntity.ok("Logged out successfully");
    }
}
