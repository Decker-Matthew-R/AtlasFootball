package com.atlas.config.oauthHandlers;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.frontend.url:}") String frontendUrl) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.info("=== OAuth2 Login Success! ===");
        log.info("User Attributes: {}", oAuth2User.getAttributes());

        UserEntity user = userService.createOrUpdateUserFromOAuth(oAuth2User, "google");

        String jwtToken = jwtTokenProvider.generateToken(user);

        log.info(
                "User processed: id={}, email={}, name={}, tokenGenerated={}",
                user.getId(),
                user.getEmail(),
                userService.getFullName(user),
                jwtToken != null);

        setJwtCookie(response, jwtToken);

        setUserInfoCookie(response, user);

        String redirectUrl = determineRedirectUrl(request);
        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String determineRedirectUrl(HttpServletRequest request) {
        if (frontendUrl != null && !frontendUrl.trim().isEmpty()) {
            return frontendUrl.endsWith("/") ? frontendUrl : frontendUrl + "/";
        }

        String referrer = request.getHeader("Referer");
        if (referrer != null) {
            if (referrer.contains(":3000")) {
                return "http://localhost:3000/";
            } else if (referrer.contains(":8080")) {
                return "http://localhost:8080/";
            }
        }

        return "http://localhost:3000/";
    }

    private void setJwtCookie(HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = new Cookie("jwt", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setDomain("localhost");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        log.info("JWT token set as HTTP-only cookie for localhost domain");
    }

    private void setUserInfoCookie(HttpServletResponse response, UserEntity user) {
        try {
            Map<String, Object> userData =
                    Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "name", userService.getFullName(user),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "profilePicture",
                                    user.getProfilePictureUrl() != null
                                            ? user.getProfilePictureUrl()
                                            : "");

            ObjectMapper objectMapper = new ObjectMapper();
            String userInfoJson = objectMapper.writeValueAsString(userData);

            String encodedUserInfo = URLEncoder.encode(userInfoJson, StandardCharsets.UTF_8);

            Cookie userCookie = new Cookie("user_info", encodedUserInfo);
            userCookie.setHttpOnly(false);
            userCookie.setSecure(false);
            userCookie.setPath("/");
            userCookie.setDomain("localhost");
            userCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userCookie);

            log.info(
                    "Enhanced user info cookie set with navbar data for user: id={}", user.getId());

        } catch (Exception e) {
            log.error("Failed to set enhanced user info cookie for user: id={}", user.getId(), e);
        }
    }
}
