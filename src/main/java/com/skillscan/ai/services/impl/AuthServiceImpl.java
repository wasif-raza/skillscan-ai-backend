package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RefreshRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.dto.response.AuthResponse;
import com.skillscan.ai.dto.response.RefreshResponse;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.exception.JwtValidationException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.CustomUserDetails;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.AuthService;
import com.skillscan.ai.services.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;



@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void register(RegisterRequest request) {

        String normalizedEmail =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .password(encoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

         log.info("User registered successfully: userId={}", user.getId());
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        String normalizedEmail =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        log.debug("Login attempt for: {}", normalizedEmail);

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        CustomUserDetails customUserDetails =
                (CustomUserDetails) auth.getPrincipal();

        User user = customUserDetails.getUser();

        String accessToken = jwt.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String refreshToken = jwt.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

         log.info("User registered successfully: userId={}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String accessToken, String refreshToken) {

        tokenBlacklistService.blacklistToken(accessToken);

        tokenBlacklistService.blacklistToken(refreshToken);
    }

    @Override
    public RefreshResponse refresh(RefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        // Validate token first
        jwt.validate(refreshToken, "refresh");

        // Then check blacklist
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new JwtValidationException("Refresh token is invalid");
        }

        // Blacklist old refresh token
       tokenBlacklistService.blacklistToken(refreshToken);

         String newAccessToken = jwt.generateAccessToken(
            jwt.getUserId(refreshToken),
            jwt.getEmail(refreshToken),
            jwt.getRole(refreshToken)
    );

    String newRefreshToken = jwt.generateRefreshToken(
            jwt.getUserId(refreshToken),
            jwt.getEmail(refreshToken),
            jwt.getRole(refreshToken)
    );

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
