package com.atlas.config.jwt;

import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract JWT token from HTTP-only cookie
            String jwtToken = extractJwtFromCookie(request);

            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                // Extract user information from valid JWT
                Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);

                log.debug("Valid JWT found for user: id={}, email={}", userId, email);

                // Load user from database to get complete user information
                Optional<UserEntity> userOptional = userService.findById(userId);

                if (userOptional.isPresent()) {
                    UserEntity user = userOptional.get();

                    // Create authentication object with user details
                    Authentication authentication = createAuthentication(user, request);

                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug(
                            "Authentication set for user: id={}, email={}",
                            user.getId(),
                            user.getEmail());
                } else {
                    log.warn("JWT valid but user not found in database: userId={}", userId);
                }
            } else {
                log.debug("No valid JWT token found in request");
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Clear any existing authentication on error
            SecurityContextHolder.clearContext();
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /** Extract JWT token from HTTP-only cookie */
    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                String token = cookie.getValue();
                log.debug("JWT token found in cookie");
                return token;
            }
        }

        log.debug("No JWT cookie found in request");
        return null;
    }

    /** Create Spring Security Authentication object from user entity */
    private Authentication createAuthentication(UserEntity user, HttpServletRequest request) {
        // Use email as principal name to avoid session indexing issues
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), // Principal - use email for session indexing
                        null, // Credentials - null since we've already validated via JWT
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_USER")) // Authorities
                        );

        // Add user entity as details for easy access
        authentication.setDetails(user);

        return authentication;
    }

    /** Skip JWT validation for public endpoints */
    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip JWT validation for OAuth and public endpoints
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/auth/login")
                || path.startsWith("/error")
                || path.startsWith("/actuator/health");
    }
}
