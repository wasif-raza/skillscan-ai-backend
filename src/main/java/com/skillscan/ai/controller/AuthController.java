package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    //  REGISTER
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req) {

        //  Use Spring Security (not manual password check)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        var user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
                "accessToken", jwt.generateAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                ),
                "refreshToken", jwt.generateRefreshToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                )
        );
    }

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
}