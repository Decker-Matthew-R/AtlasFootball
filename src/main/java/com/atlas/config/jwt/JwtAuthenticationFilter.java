package com.atlas.config.jwt;

import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.annotation.Nonnull;
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
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwtToken = extractJwtFromCookie(request);

            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);

                Optional<UserEntity> userOptional = userService.findById(userId);

                if (userOptional.isPresent()) {
                    UserEntity user = userOptional.get();

                    Authentication authentication = createAuthentication(user, request);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    log.warn("JWT valid but user not found in database: userId={}", userId);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private Authentication createAuthentication(UserEntity user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        authentication.setDetails(user);

        return authentication;
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/auth/login")
                || path.startsWith("/error");
    }
}
