// Create this file: src/main/java/com/atlas/controller/TestAuthController.java
package com.atlas.testUtils.controller;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.user.repository.UserRepository;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Profile("journey")
@Slf4j
public class TestAuthController {

    @Autowired private UserRepository userRepository;

    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Autowired private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> testLogin(
            @RequestBody(required = false) Map<String, String> request,
            HttpServletResponse response) {

        try {
            log.info("=== Test Login Request ===");

            UserEntity testUser = createOrFindTestUser(request);

            String jwtToken = jwtTokenProvider.generateToken(testUser);

            log.info(
                    "Test user processed: id={}, email={}, name={}, tokenGenerated={}",
                    testUser.getId(),
                    testUser.getEmail(),
                    userService.getFullName(testUser),
                    jwtToken != null);

            setJwtCookie(response, jwtToken);

            setUserInfoCookie(response, testUser);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Test login successful");
            responseBody.put("user", createUserResponse(testUser));

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            log.error("Test login failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Test login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> testLogout(HttpServletResponse response) {
        log.info("=== Test Logout Request ===");

        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setDomain("localhost");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        Cookie userCookie = new Cookie("user_info", null);
        userCookie.setHttpOnly(false);
        userCookie.setSecure(false);
        userCookie.setPath("/");
        userCookie.setDomain("localhost");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Test logout successful");
        return ResponseEntity.ok(responseBody);
    }

    private UserEntity createOrFindTestUser(Map<String, String> request) {
        String email = request != null ? request.get("email") : "test-user@example.com";
        String name = request != null ? request.get("name") : "Test User";

        try {
            Optional<UserEntity> existingUserOpt = userRepository.findByEmail(email);
            if (existingUserOpt.isPresent()) {
                UserEntity existingUser = existingUserOpt.get();
                log.info("Found existing test user: {}", email);
                // Update last login
                existingUser.updateLastLogin();
                return userRepository.save(existingUser);
            }
        } catch (Exception e) {
            log.debug("User not found, will create new one");
        }

        UserEntity testUser = new UserEntity();
        testUser.setEmail(email);

        String[] nameParts = name.split(" ", 2);
        testUser.setFirstName(nameParts[0]);
        testUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        testUser.setProfilePictureUrl("https://example.com/test-avatar.jpg");

        try {
            testUser = userRepository.save(testUser);
            testUser.updateLastLogin();
            testUser = userRepository.save(testUser);

            log.info(
                    "Created new test user: id={}, email={}",
                    testUser.getId(),
                    testUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to save test user", e);
            throw new RuntimeException("Failed to create test user", e);
        }

        return testUser;
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

    private Map<String, Object> createUserResponse(UserEntity user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("name", userService.getFullName(user));
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("profilePicture", user.getProfilePictureUrl());
        return userResponse;
    }
}
