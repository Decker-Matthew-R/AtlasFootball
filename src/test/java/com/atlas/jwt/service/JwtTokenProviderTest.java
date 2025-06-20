package com.atlas.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.atlas.user.repository.model.UserEntity;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        String testSecret = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ab";
        int expirationMs = 3600000;
        jwtTokenProvider = new JwtTokenProvider(testSecret, expirationMs);

        testUser = new UserEntity();
        testUser.setId(123L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setProfilePictureUrl("https://example.com/profile.jpg");
    }

    @Test
    void generateToken_shouldCreateValidJwtToken() {
        String token = jwtTokenProvider.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_shouldIncludeUserClaims() {
        String token = jwtTokenProvider.generateToken(testUser);

        // Then
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(123L);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_shouldHandleUserWithoutProfilePicture() {
        testUser.setProfilePictureUrl(null);

        String token = jwtTokenProvider.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(123L);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_shouldThrowExceptionForNullUser() {
        assertThatThrownBy(() -> jwtTokenProvider.generateToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null");
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtTokenProvider.generateToken(testUser);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("   ");

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnFalseForFreshToken() {
        String token = jwtTokenProvider.generateToken(testUser);

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnTrueForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);

        assertThat(isExpired).isTrue(); // Invalid tokens are treated as expired
    }

    @Test
    void getExpirationDateFromToken_shouldReturnFutureDate() {
        String token = jwtTokenProvider.generateToken(testUser);

        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    void getUserIdFromToken_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
                .isInstanceOf(JwtTokenParsingException.class)
                .hasMessage("Failed to extract user ID from token");
    }

    @Test
    void getEmailFromToken_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getEmailFromToken(invalidToken))
                .isInstanceOf(JwtTokenParsingException.class)
                .hasMessage("Failed to extract email from token");
    }

    @Test
    void getExpirationDateFromToken_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getExpirationDateFromToken(invalidToken))
                .isInstanceOf(JwtTokenParsingException.class)
                .hasMessage("Failed to extract expiration date from token");
    }

    @Test
    void generateToken_shouldCreateDifferentTokensForSameUser() throws InterruptedException {
        String token1 = jwtTokenProvider.generateToken(testUser);

        Thread.sleep(1100); // 1.1 seconds to be safe

        String token2 = jwtTokenProvider.generateToken(testUser);

        assertThat(token1).isNotEqualTo(token2);

        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();

        assertThat(jwtTokenProvider.getUserIdFromToken(token1)).isEqualTo(123L);
        assertThat(jwtTokenProvider.getUserIdFromToken(token2)).isEqualTo(123L);
    }

    @Test
    void generateToken_shouldCreateDifferentTokensForDifferentUsers() {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(456L);
        anotherUser.setEmail("another@example.com");
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");

        String token1 = jwtTokenProvider.generateToken(testUser);
        String token2 = jwtTokenProvider.generateToken(anotherUser);

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUserIdFromToken(token1)).isEqualTo(123L);
        assertThat(jwtTokenProvider.getUserIdFromToken(token2)).isEqualTo(456L);
    }

    @Test
    void getExpirationTimeMs_shouldReturnConfiguredValue() {
        int expirationMs = jwtTokenProvider.getExpirationTimeMs();

        assertThat(expirationMs).isEqualTo(3600000); // 1 hour as configured in setUp
    }

    @Test
    void generateToken_shouldHandleUserWithMinimalData() {
        UserEntity minimalUser = new UserEntity();
        minimalUser.setId(999L);
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setFirstName("Min");
        minimalUser.setLastName("User");

        String token = jwtTokenProvider.generateToken(minimalUser);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(999L);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("minimal@example.com");
    }
}
