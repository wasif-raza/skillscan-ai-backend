package com.skillscan.ai.security;

import com.skillscan.ai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email must not be empty");
                   }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        var user = repo.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalized));
        return new CustomUserDetails(user);
    }
}