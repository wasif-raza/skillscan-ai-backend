package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo ;
    private final PasswordEncoder encoder;
    private  final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;


    @Override
    public void register(RegisterRequest request) {

        if (repo.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(encoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();
        repo.save(user);

    }



    @Override
    public String login(LoginRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow();

        return jwt.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()

        );
    }
}
