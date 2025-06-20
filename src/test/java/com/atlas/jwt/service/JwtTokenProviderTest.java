package com.atlas.jwt.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.spy;

import com.atlas.config.jwt.JwtExceptions.JwtTokenGenerationException;
import com.atlas.config.jwt.JwtExceptions.JwtTokenParsingException;
import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.user.repository.model.UserEntity;
import java.lang.reflect.Field;
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

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_shouldIncludeUserClaims() {
        String token = jwtTokenProvider.generateToken(testUser);

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

        assertThat(isExpired).isTrue();
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

        Thread.sleep(1100);

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

        assertThat(expirationMs).isEqualTo(3600000);
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

    @Test
    void generateToken_shouldThrowJwtTokenGenerationExceptionWhenUserIdIsNull() {
        UserEntity userWithNullId = new UserEntity();
        userWithNullId.setId(null);
        userWithNullId.setEmail("test@example.com");
        userWithNullId.setFirstName("John");
        userWithNullId.setLastName("Doe");

        assertThatThrownBy(() -> jwtTokenProvider.generateToken(userWithNullId))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessage("Failed to generate JWT token")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void validateToken_shouldReturnFalseOnJwtException() {
        // gitleaks ignore-line
        // gitleaks ignore
        String mockTestSecretWithWrongSignature =
                "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEyMywiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.wrong_signature_here";

        boolean isValid = jwtTokenProvider.validateToken(mockTestSecretWithWrongSignature);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldHandleCompletelyMalformedTokens() {
        String[] malformedTokens = {
            "not-a-jwt",
            "only.two.parts",
            "too.many.parts.in.this.jwt.token",
            "",
            "   ",
            null,
            "header.body.signature.extra",
            "🤔.🚀.💥"
        };

        for (String malformedToken : malformedTokens) {
            boolean isValid = jwtTokenProvider.validateToken(malformedToken);
            assertThat(isValid).as("Token '%s' should be invalid", malformedToken).isFalse();
        }
    }

    @Test
    void generateToken_shouldCatchExceptionAndWrapInJwtTokenGenerationException() {
        UserEntity userWithNullId = new UserEntity();
        userWithNullId.setId(null);
        userWithNullId.setEmail("test@example.com");
        userWithNullId.setFirstName("John");
        userWithNullId.setLastName("Doe");

        assertThatThrownBy(() -> jwtTokenProvider.generateToken(userWithNullId))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessage("Failed to generate JWT token")
                .hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    void generateToken_shouldHandleUnexpectedExceptionsDuringTokenCreation() throws Exception {
        JwtTokenProvider spyProvider = spy(jwtTokenProvider);

        UserEntity testUser = new UserEntity();
        testUser.setId(123L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        Field secretKeyField = JwtTokenProvider.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(spyProvider, null); // Set secretKey to null to cause exception

        assertThatThrownBy(() -> spyProvider.generateToken(testUser))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessage("Failed to generate JWT token");
    }

    @Test
    void generateToken_shouldHandleExtremeUserData() {
        UserEntity extremeUser = new UserEntity();
        extremeUser.setId(Long.MAX_VALUE);
        extremeUser.setEmail("a".repeat(10000) + "@example.com");
        extremeUser.setFirstName("🚀".repeat(1000));
        extremeUser.setLastName("💥".repeat(1000));
        assertThatCode(
                        () -> {
                            String token = jwtTokenProvider.generateToken(extremeUser);
                            assertThat(token).isNotNull();
                            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
                        })
                .doesNotThrowAnyException();
    }

    // Test the validateToken catch blocks more reliably
    @Test
    void validateToken_shouldHandleJwtExceptionInCatchBlock() {
        String header = "eyJhbGciOiJIUzUxMiJ9";
        String payload = "eyJ1c2VySWQiOjEyMywiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIn0";
        String wrongSignature = "wrong_signature_that_will_fail_validation";
        String tokenWithWrongSig = header + "." + payload + "." + wrongSignature;

        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSig);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldHandleGeneralExceptionInCatchBlock() {
        String[] problematicTokens = {
            "header.payload",
            "a.b.c.d.e",
            "🤔.🚀.💥",
            new String(new byte[] {(byte) 0xFF, (byte) 0xFE}),
            "header." + "x".repeat(100000) + ".signature"
        };

        for (String problematicToken : problematicTokens) {
            boolean isValid = jwtTokenProvider.validateToken(problematicToken);
            assertThat(isValid)
                    .as(
                            "Problematic token should be invalid: %s",
                            problematicToken.length() > 50
                                    ? problematicToken.substring(0, 50) + "..."
                                    : problematicToken)
                    .isFalse();
        }
    }
}
