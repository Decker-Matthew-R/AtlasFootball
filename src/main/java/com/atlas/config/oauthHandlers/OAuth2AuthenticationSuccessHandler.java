package com.atlas.config.oauthHandlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Log user details for debugging
        logger.info("=== OAuth2 Login Success! ===");
        logger.info("User Attributes: {}", oAuth2User.getAttributes());

        // Extract user information
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub");

        logger.info("Email: {}", email);
        logger.info("Name: {}", name);
        logger.info("Google Sub: {}", sub);
        logger.info("Profile Picture: {}", picture);

        // For now, redirect to a success page (you'll update this later for JWT)
        response.sendRedirect(
                "http://localhost:3000/oauth-success?email=" + email + "&name=" + name);
    }
}
