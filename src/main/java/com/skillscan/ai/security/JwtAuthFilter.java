package com.skillscan.ai.security;

import com.skillscan.ai.services.TokenBlacklistService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        // ✅ No token → continue
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);

        try {

            // ✅ FIX 1: Handle blacklist WITHOUT exception
            if (tokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            jwt.validate(token, "access");

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwt.getEmail(token);

                var userDetails = userDetailsService.loadUserByUsername(email);

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // ✅ Optional but recommended
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            // ✅ FIX 2: DO NOT force response status
            SecurityContextHolder.clearContext();
        }

        // ✅ ALWAYS continue
        chain.doFilter(req, res);
    }
}