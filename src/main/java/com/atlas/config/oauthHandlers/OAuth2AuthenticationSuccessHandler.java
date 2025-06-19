package com.atlas.config.oauthHandlers;

import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        logger.info("=== OAuth2 Login Success! ===");
        logger.info("User Attributes: {}", oAuth2User.getAttributes());

        // Create or update user using UserService
        UserEntity user = userService.createOrUpdateUserFromOAuth(oAuth2User, "google");

        logger.info(
                "User processed: id={}, email={}, name={}",
                user.getId(),
                user.getEmail(),
                userService.getFullName(user));

        // Redirect to frontend with user info
        String redirectUrl =
                String.format(
                        "http://localhost:3000/oauth-success?userId=%d&email=%s&name=%s",
                        user.getId(), user.getEmail(), userService.getFullName(user));
        response.sendRedirect(redirectUrl);
    }
}
