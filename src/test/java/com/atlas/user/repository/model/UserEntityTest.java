package com.atlas.user.repository.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

import com.atlas.user.repository.UserOAuthProvider;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setEmail("[email protected]");
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
    }

    @Test
    void onCreate_shouldSetCreatedAtAndUpdatedAtTimestamps() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        userEntity.onCreate();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(userEntity.getCreatedAt()).isNotNull();
        assertThat(userEntity.getUpdatedAt()).isNotNull();
        assertThat(userEntity.getCreatedAt()).isBetween(before, after);
        assertThat(userEntity.getUpdatedAt()).isBetween(before, after);
        assertThat(userEntity.getCreatedAt())
                .isCloseTo(userEntity.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void onUpdate_shouldUpdateUpdatedAtTimestamp() {
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusMinutes(30);

        userEntity.setCreatedAt(originalCreatedAt);
        userEntity.setUpdatedAt(originalUpdatedAt);

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        userEntity.onUpdate();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(userEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(userEntity.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);
        assertThat(userEntity.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void updateLastLogin_shouldSetLastLoginToCurrentTime() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        userEntity.updateLastLogin();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(userEntity.getLastLogin()).isNotNull();
        assertThat(userEntity.getLastLogin()).isBetween(before, after);
    }

    @Test
    void updateLastLogin_shouldUpdateExistingLastLogin() {
        LocalDateTime originalLastLogin = LocalDateTime.now().minusHours(1);
        userEntity.setLastLogin(originalLastLogin);

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        userEntity.updateLastLogin();

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(userEntity.getLastLogin()).isNotEqualTo(originalLastLogin);
        assertThat(userEntity.getLastLogin()).isBetween(before, after);
    }

    @Test
    void addOAuthProvider_shouldAddProviderToListAndSetBackReference() {
        // Given
        UserOAuthProvider oauthProvider = new UserOAuthProvider();
        oauthProvider.setProviderName("google");
        oauthProvider.setProviderUserId("google123");
        oauthProvider.setProviderEmail("[email protected]");

        userEntity.addOAuthProvider(oauthProvider);

        assertThat(userEntity.getOauthProviders()).hasSize(1);
        assertThat(userEntity.getOauthProviders()).contains(oauthProvider);
        assertThat(oauthProvider.getUser()).isEqualTo(userEntity);
    }

    @Test
    void addOAuthProvider_shouldHandleMultipleProviders() {
        UserOAuthProvider googleProvider = new UserOAuthProvider();
        googleProvider.setProviderName("google");
        googleProvider.setProviderUserId("google123");

        UserOAuthProvider githubProvider = new UserOAuthProvider();
        githubProvider.setProviderName("github");
        githubProvider.setProviderUserId("github456");

        userEntity.addOAuthProvider(googleProvider);
        userEntity.addOAuthProvider(githubProvider);

        assertThat(userEntity.getOauthProviders()).hasSize(2);
        assertThat(userEntity.getOauthProviders()).containsExactly(googleProvider, githubProvider);
        assertThat(googleProvider.getUser()).isEqualTo(userEntity);
        assertThat(githubProvider.getUser()).isEqualTo(userEntity);
    }

    @Test
    void addOAuthProvider_shouldMaintainBidirectionalRelationship() {
        UserOAuthProvider oauthProvider = new UserOAuthProvider();
        oauthProvider.setProviderName("google");

        userEntity.addOAuthProvider(oauthProvider);

        assertThat(userEntity.getOauthProviders()).contains(oauthProvider);
        assertThat(oauthProvider.getUser()).isEqualTo(userEntity);

        assertThat(oauthProvider.getUser().getOauthProviders()).contains(oauthProvider);
    }

    @Test
    void newUserEntity_shouldHaveEmptyOAuthProvidersList() {
        UserEntity newUser = new UserEntity();

        assertThat(newUser.getOauthProviders()).isNotNull();
        assertThat(newUser.getOauthProviders()).isEmpty();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        Long id = 1L;
        String email = "[email protected]";
        String firstName = "Jane";
        String lastName = "Smith";
        String profilePictureUrl = "https://example.com/pic.jpg";
        LocalDateTime now = LocalDateTime.now();

        UserEntity user =
                new UserEntity(
                        id,
                        email,
                        firstName,
                        lastName,
                        profilePictureUrl,
                        now,
                        now,
                        now,
                        new ArrayList<>());

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getLastName()).isEqualTo(lastName);
        assertThat(user.getProfilePictureUrl()).isEqualTo(profilePictureUrl);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
        assertThat(user.getLastLogin()).isEqualTo(now);
        assertThat(user.getOauthProviders()).isNotNull();
    }

    @Test
    void addOAuthProvider_withNullProvider_shouldHandleGracefully() {
        UserOAuthProvider nullProvider = null;

        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    userEntity.addOAuthProvider(nullProvider);
                });
    }

    @Test
    void lifecycleCallbacks_shouldBeCalledInCorrectOrder() {
        UserEntity user = new UserEntity();
        user.setEmail("[email protected]");
        user.setFirstName("Test");
        user.setLastName("User");

        user.onCreate();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            /* ignore */
        }

        user.onUpdate();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}
