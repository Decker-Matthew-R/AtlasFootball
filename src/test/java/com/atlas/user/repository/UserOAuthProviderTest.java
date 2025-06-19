package com.atlas.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.atlas.user.repository.model.UserEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserOAuthProviderTest {

    private UserOAuthProvider userOAuthProvider;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("[email protected]");
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");

        userOAuthProvider = new UserOAuthProvider();
        userOAuthProvider.setUser(userEntity);
        userOAuthProvider.setProviderName("google");
        userOAuthProvider.setProviderUserId("google123456");
        userOAuthProvider.setProviderEmail("[email protected]");
    }

    @Test
    void onCreate_shouldSetLinkedAtAndLastUsedWhenBothAreNull() {
        LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        userOAuthProvider.setLinkedAt(null);
        userOAuthProvider.setLastUsed(null);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

            userOAuthProvider.onCreate();

            assertThat(userOAuthProvider.getLinkedAt()).isEqualTo(fixedTime);
            assertThat(userOAuthProvider.getLastUsed()).isEqualTo(fixedTime);
        }
    }

    @Test
    void onCreate_shouldNotOverrideExistingLinkedAt() {
        LocalDateTime existingLinkedAt = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        LocalDateTime currentTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        userOAuthProvider.setLinkedAt(existingLinkedAt);
        userOAuthProvider.setLastUsed(null);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(currentTime);

            userOAuthProvider.onCreate();

            assertThat(userOAuthProvider.getLinkedAt()).isEqualTo(existingLinkedAt);
            assertThat(userOAuthProvider.getLastUsed()).isEqualTo(currentTime);
        }
    }

    @Test
    void onCreate_shouldNotOverrideExistingLastUsed() {
        LocalDateTime existingLastUsed = LocalDateTime.of(2025, 5, 1, 14, 30, 0);
        LocalDateTime currentTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        userOAuthProvider.setLinkedAt(null);
        userOAuthProvider.setLastUsed(existingLastUsed);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(currentTime);

            userOAuthProvider.onCreate();

            assertThat(userOAuthProvider.getLinkedAt()).isEqualTo(currentTime);
            assertThat(userOAuthProvider.getLastUsed()).isEqualTo(existingLastUsed);
        }
    }

    @Test
    void onCreate_shouldNotOverrideBothWhenBothExist() {
        LocalDateTime existingLinkedAt = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        LocalDateTime existingLastUsed = LocalDateTime.of(2025, 5, 1, 14, 30, 0);
        LocalDateTime currentTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        userOAuthProvider.setLinkedAt(existingLinkedAt);
        userOAuthProvider.setLastUsed(existingLastUsed);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(currentTime);

            userOAuthProvider.onCreate();

            assertThat(userOAuthProvider.getLinkedAt())
                    .isEqualTo(existingLinkedAt); // Should not change
            assertThat(userOAuthProvider.getLastUsed())
                    .isEqualTo(existingLastUsed); // Should not change
        }
    }

    @Test
    void noArgsConstructor_shouldSetDefaultValues() {
        UserOAuthProvider provider = new UserOAuthProvider();

        assertThat(provider.getId()).isNull();
        assertThat(provider.getUser()).isNull();
        assertThat(provider.getProviderName()).isNull();
        assertThat(provider.getProviderUserId()).isNull();
        assertThat(provider.getProviderEmail()).isNull();
        assertThat(provider.getLinkedAt()).isNull();
        assertThat(provider.getLastUsed()).isNull();
        assertThat(provider.getProviderUsername()).isNull();
        assertThat(provider.getIsPrimary()).isFalse(); // Default value
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        Long id = 1L;
        String providerName = "github";
        String providerUserId = "github789";
        String providerEmail = "[email protected]";
        LocalDateTime linkedAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime lastUsed = LocalDateTime.of(2025, 6, 1, 15, 30, 0);
        String providerUsername = "johndoe";
        Boolean isPrimary = true;

        UserOAuthProvider provider =
                new UserOAuthProvider(
                        id,
                        userEntity,
                        providerName,
                        providerUserId,
                        providerEmail,
                        linkedAt,
                        lastUsed,
                        providerUsername,
                        isPrimary);

        assertThat(provider.getId()).isEqualTo(id);
        assertThat(provider.getUser()).isEqualTo(userEntity);
        assertThat(provider.getProviderName()).isEqualTo(providerName);
        assertThat(provider.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(provider.getProviderEmail()).isEqualTo(providerEmail);
        assertThat(provider.getLinkedAt()).isEqualTo(linkedAt);
        assertThat(provider.getLastUsed()).isEqualTo(lastUsed);
        assertThat(provider.getProviderUsername()).isEqualTo(providerUsername);
        assertThat(provider.getIsPrimary()).isEqualTo(isPrimary);
    }

    @Test
    void isPrimary_shouldDefaultToFalse() {
        UserOAuthProvider provider = new UserOAuthProvider();

        assertThat(provider.getIsPrimary()).isFalse();
    }

    @Test
    void isPrimary_canBeSetToTrue() {
        UserOAuthProvider provider = new UserOAuthProvider();

        provider.setIsPrimary(true);

        assertThat(provider.getIsPrimary()).isTrue();
    }

    @Test
    void setUser_shouldEstablishRelationship() {
        UserOAuthProvider provider = new UserOAuthProvider();

        provider.setUser(userEntity);

        assertThat(provider.getUser()).isEqualTo(userEntity);
    }

    @Test
    void setUser_shouldAllowNullUser() {
        userOAuthProvider.setUser(userEntity);

        userOAuthProvider.setUser(null);

        assertThat(userOAuthProvider.getUser()).isNull();
    }

    @Test
    void providerFields_shouldAcceptValidData() {
        String providerName = "apple";
        String providerUserId = "apple.id.123456789";
        String providerEmail = "[email protected]";
        String providerUsername = "john.doe.apple";

        userOAuthProvider.setProviderName(providerName);
        userOAuthProvider.setProviderUserId(providerUserId);
        userOAuthProvider.setProviderEmail(providerEmail);
        userOAuthProvider.setProviderUsername(providerUsername);

        assertThat(userOAuthProvider.getProviderName()).isEqualTo(providerName);
        assertThat(userOAuthProvider.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(userOAuthProvider.getProviderEmail()).isEqualTo(providerEmail);
        assertThat(userOAuthProvider.getProviderUsername()).isEqualTo(providerUsername);
    }

    @Test
    void providerFields_shouldAcceptNullValues() {
        userOAuthProvider.setProviderEmail("[email protected]");
        userOAuthProvider.setProviderUsername("username");

        userOAuthProvider.setProviderEmail(null);
        userOAuthProvider.setProviderUsername(null);

        assertThat(userOAuthProvider.getProviderEmail()).isNull();
        assertThat(userOAuthProvider.getProviderUsername()).isNull();
    }

    @Test
    void timestamps_shouldAcceptValidDates() {
        LocalDateTime linkedAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime lastUsed = LocalDateTime.of(2025, 6, 19, 15, 30, 45);

        userOAuthProvider.setLinkedAt(linkedAt);
        userOAuthProvider.setLastUsed(lastUsed);

        assertThat(userOAuthProvider.getLinkedAt()).isEqualTo(linkedAt);
        assertThat(userOAuthProvider.getLastUsed()).isEqualTo(lastUsed);
    }

    @Test
    void lastUsed_canBeAfterLinkedAt() {
        LocalDateTime linkedAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime lastUsed = LocalDateTime.of(2025, 6, 19, 15, 30, 45);

        userOAuthProvider.setLinkedAt(linkedAt);
        userOAuthProvider.setLastUsed(lastUsed);

        assertThat(userOAuthProvider.getLastUsed()).isAfter(userOAuthProvider.getLinkedAt());
    }

    @Test
    void onCreate_behaviorForDifferentProviders() {
        LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        String[] providers = {"google", "github", "apple", "microsoft", "facebook"};

        for (String provider : providers) {
            UserOAuthProvider oauthProvider = new UserOAuthProvider();
            oauthProvider.setProviderName(provider);
            oauthProvider.setProviderUserId(provider + "123");

            try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

                oauthProvider.onCreate();

                assertThat(oauthProvider.getLinkedAt()).isEqualTo(fixedTime);
                assertThat(oauthProvider.getLastUsed()).isEqualTo(fixedTime);
            }
        }
    }

    @Test
    void onCreate_withFutureTimestamps_shouldNotOverride() {
        LocalDateTime futureLinkedAt = LocalDateTime.of(2030, 1, 1, 12, 0, 0);
        LocalDateTime futureLastUsed = LocalDateTime.of(2030, 6, 1, 14, 30, 0);
        LocalDateTime currentTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        userOAuthProvider.setLinkedAt(futureLinkedAt);
        userOAuthProvider.setLastUsed(futureLastUsed);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(currentTime);

            userOAuthProvider.onCreate();

            assertThat(userOAuthProvider.getLinkedAt()).isEqualTo(futureLinkedAt);
            assertThat(userOAuthProvider.getLastUsed()).isEqualTo(futureLastUsed);
        }
    }

    @Test
    void integration_withUserEntityAddOAuthProvider() {
        UserEntity user = new UserEntity();
        user.setEmail("[email protected]");
        user.setFirstName("Jane");
        user.setLastName("Smith");

        UserOAuthProvider provider = new UserOAuthProvider();
        provider.setProviderName("google");
        provider.setProviderUserId("google987654");

        user.addOAuthProvider(provider);

        assertThat(provider.getUser()).isEqualTo(user);
        assertThat(user.getOauthProviders()).contains(provider);
    }
}
