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
            String jwtToken = extractJwtFromCookie(request);

            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);

                log.debug("Valid JWT found for user: id={}, email={}", userId, email);

                Optional<UserEntity> userOptional = userService.findById(userId);

                if (userOptional.isPresent()) {
                    UserEntity user = userOptional.get();

                    Authentication authentication = createAuthentication(user, request);

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
            SecurityContextHolder.clearContext();
        }

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
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        authentication.setDetails(user);

        return authentication;
    }

    /** Skip JWT validation for public endpoints */
    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/auth/login")
                || path.startsWith("/error")
                || path.startsWith("/actuator/health");
    }
}
