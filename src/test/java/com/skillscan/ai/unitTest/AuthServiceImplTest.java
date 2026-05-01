package com.skillscan.ai.unitTest;

import com.skillscan.ai.dto.request.LoginRequest;
import com.skillscan.ai.dto.request.RegisterRequest;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.security.JwtTokenProvider;
import com.skillscan.ai.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtTokenProvider jwt;

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================
    // REGISTER TESTS
    // =========================

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Wasif");
        request.setLastName("Raza");
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        when(repo.existsByEmail("test@mail.com")).thenReturn(false);
        when(encoder.encode("123456")).thenReturn("encoded-password");

        authService.register(request);

        verify(repo, times(1)).save(argThat(user ->
                user.getEmail().equals("test@mail.com") &&
                        user.getFirstName().equals("Wasif") &&
                        user.getLastName().equals("Raza") &&
                        user.getPassword().equals("encoded-password") &&
                        user.getRole() == UserRole.USER
        ));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");

        when(repo.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(request));

        verify(repo, never()).save(any());
    }

    // =========================
    // LOGIN TESTS
    // =========================

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .role(UserRole.USER)
                .build();

        when(authManager.authenticate(any())).thenReturn(null);
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(jwt.generateAccessToken(any(), any(), any()))
                .thenReturn("mock-token");

        String token = authService.login(request);

        assertEquals("mock-token", token);

        verify(authManager, times(1)).authenticate(any());
        verify(jwt, times(1))
                .generateAccessToken(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        when(authManager.authenticate(any())).thenReturn(null);
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> authService.login(request));
    }
}