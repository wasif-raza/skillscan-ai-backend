package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
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
    public Map<String, String> login(@RequestBody LoginRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        var user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> response = new java.util.LinkedHashMap<>();

        response.put("accessToken", jwt.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        ));

        response.put("refreshToken", jwt.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        ));

        return response;
    }
    //  REFRESH
    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestParam String refreshToken) {

        jwt.validate(refreshToken, "refresh");

        return Map.of(
                "accessToken", jwt.generateAccessToken(
                        jwt.getUserId(refreshToken),
                        jwt.getEmail(refreshToken),
                        jwt.getRole(refreshToken)
                )
        );
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token found");
        }

        String token = authHeader.substring(7);

        tokenBlacklistService.blacklistToken(token);

        return ResponseEntity.ok("Logged out successfully");
    }
}