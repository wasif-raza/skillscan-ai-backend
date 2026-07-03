package com.skillscan.ai.config;

import com.skillscan.ai.security.CustomUserDetailsService;
import com.skillscan.ai.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter filter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain chain(
            HttpSecurity http
    ) throws Exception {

        return http

                .cors(cors -> {})

                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .sessionManagement(
                        sm -> sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(
                        auth -> auth

                                // auth

                                .requestMatchers(
                                        "/api/auth/register",
                                        "/api/auth/login",
                                        "/api/auth/refresh"
                                ).permitAll()

                                .requestMatchers(
                                        "/api/auth/logout"
                                ).authenticated()


                                // guest analysis

                                .requestMatchers(
                                        "/api/resumes/**"
                                ).permitAll()

                                .requestMatchers(
                                        "/api/analysis/**"
                                ).permitAll()


                                // everything else

                                .anyRequest()
                                .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .addFilterBefore(
                        filter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:3002"
                )
        );

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        config.setAllowedHeaders(
                List.of("*")
        );

        config.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }

    @Bean
    public AuthenticationProvider
    authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService
        );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    @Bean
    public PasswordEncoder
    passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean
    public AuthenticationManager
    authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config
                .getAuthenticationManager();

    }
}