package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req) {

        var user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return Map.of(
                "accessToken", jwt.generateAccessToken(user.getId(), user.getRole()),
                "refreshToken", jwt.generateRefreshToken(user.getId(), user.getRole())
        );
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestParam String refreshToken) {

        jwt.validate(refreshToken, "refresh");

        return Map.of(
                "accessToken", jwt.generateAccessToken(
                        jwt.getUserId(refreshToken),
                        jwt.getRole(refreshToken)
                )
        );
    }
}