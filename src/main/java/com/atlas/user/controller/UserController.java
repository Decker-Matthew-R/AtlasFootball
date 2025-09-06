package com.atlas.user.controller;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.service.MetricsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class UserController {
    private final MetricsService metricsService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            Cookie jwtCookie = new Cookie("jwt", null);

            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setDomain("localhost");
            jwtCookie.setMaxAge(0);
            response.addCookie(jwtCookie);

            captureLogoutMetric(request);

            return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed"));
        }
    }

    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    private void captureLogoutMetric(HttpServletRequest request) {
        try {
            String jwtToken = extractJwtFromCookie(request);
            if (jwtToken != null) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);

                JSONObject eventMetadata = new JSONObject();
                eventMetadata.put("triggerId", "Logout Success");
                eventMetadata.put("screen", "N/A");

                MetricEventDTO logoutMetric =
                        MetricEventDTO.builder()
                                .event(MetricEventType.LOGOUT)
                                .eventMetadata(eventMetadata)
                                .userId(userId)
                                .build();

                metricsService.saveMetricEvent(logoutMetric);
            }
        } catch (Exception e) {
            log.error("Failed to capture logout metric", e);
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
