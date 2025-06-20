package com.atlas.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atlas.user.repository.UserOAuthProviderRepository;
import com.atlas.user.repository.UserRepository;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.repository.model.UserOAuthProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private UserOAuthProviderRepository oauthProviderRepository;

    @Mock private OAuth2User oAuth2User;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, oauthProviderRepository);
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldCreateNewUserWhenNoneExists() {
        String email = "[email protected]";
        String name = "John Doe";
        String providerUserId = "google123";
        String profilePicture = "https://example.com/pic.jpg";
        String providerName = "google";

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(profilePicture);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setEmail(email);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo(email);
        assertThat(capturedUser.getFirstName()).isEqualTo("John");
        assertThat(capturedUser.getLastName()).isEqualTo("Doe");
        assertThat(capturedUser.getProfilePictureUrl()).isEqualTo(profilePicture);

        verify(oauthProviderRepository).save(any(UserOAuthProvider.class));
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldUpdateExistingUserWhenOAuthProviderExists() {
        String email = "[email protected]";
        String name = "John Updated";
        String providerUserId = "google123";
        String providerName = "google";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        UserOAuthProvider existingOAuth = new UserOAuthProvider();
        existingOAuth.setUser(existingUser);
        existingOAuth.setProviderName(providerName);
        existingOAuth.setProviderUserId(providerUserId);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn("https://new-pic.com/pic.jpg");

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.of(existingOAuth));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result).isEqualTo(existingUser);

        verify(userRepository).save(existingUser);
        verify(oauthProviderRepository).save(existingOAuth);
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldLinkOAuthToExistingUserByEmail() {
        String email = "[email protected]";
        String name = "John Doe";
        String providerUserId = "github456";
        String providerName = "github";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        ArgumentCaptor<UserOAuthProvider> oauthCaptor =
                ArgumentCaptor.forClass(UserOAuthProvider.class);
        verify(oauthProviderRepository).save(oauthCaptor.capture());
        UserOAuthProvider capturedOAuth = oauthCaptor.getValue();

        assertThat(capturedOAuth.getUser()).isEqualTo(existingUser);
        assertThat(capturedOAuth.getProviderName()).isEqualTo(providerName);
        assertThat(capturedOAuth.getProviderUserId()).isEqualTo(providerUserId);
    }

    @Test
    void createNewUserFromOAuth_shouldParseFullNameCorrectly() {
        when(oAuth2User.getAttribute("email")).thenReturn("[email protected]");
        when(oAuth2User.getAttribute("name")).thenReturn("John Michael Doe");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://pic.com/pic.jpg");
        when(oAuth2User.getAttribute("sub")).thenReturn("google123");

        UserEntity result = invokeCreateNewUserFromOAuth();

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Michael Doe");
    }

    @Test
    void createNewUserFromOAuth_shouldHandleSingleName() {
        when(oAuth2User.getAttribute("email")).thenReturn("[email protected]");
        when(oAuth2User.getAttribute("name")).thenReturn("Madonna");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn("google123");

        UserEntity result = invokeCreateNewUserFromOAuth();

        assertThat(result.getFirstName()).isEqualTo("Madonna");
        assertThat(result.getLastName()).isEqualTo("");
    }

    @Test
    void createNewUserFromOAuth_shouldHandleNullName() {
        when(oAuth2User.getAttribute("email")).thenReturn("[email protected]");
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn("google123");

        UserEntity result = invokeCreateNewUserFromOAuth();

        assertThat(result.getFirstName()).isEqualTo("Unknown");
        assertThat(result.getLastName()).isEqualTo("");
    }

    @Test
    void createNewUserFromOAuth_shouldHandleEmptyName() {
        when(oAuth2User.getAttribute("email")).thenReturn("[email protected]");
        when(oAuth2User.getAttribute("name")).thenReturn("   ");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn("google123");

        UserEntity result = invokeCreateNewUserFromOAuth();

        assertThat(result.getFirstName()).isEqualTo("Unknown");
        assertThat(result.getLastName()).isEqualTo("");
    }

    @Test
    void linkOAuthProvider_shouldSetPrimaryTrueForFirstProvider() {
        LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 19, 10, 30, 45);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        "google", "google123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("[email protected]")).thenReturn(Optional.empty());

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setEmail("[email protected]");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        MockedStatic<LocalDateTime> mockedTime = null;
        try {
            mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
            mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

            userService.createOrUpdateUserFromOAuth(createMockOAuth2User(), "google");
        } finally {
            if (mockedTime != null) {
                mockedTime.close();
            }
        }

        ArgumentCaptor<UserOAuthProvider> oauthCaptor =
                ArgumentCaptor.forClass(UserOAuthProvider.class);
        verify(oauthProviderRepository).save(oauthCaptor.capture());
        UserOAuthProvider capturedOAuth = oauthCaptor.getValue();

        assertThat(capturedOAuth.getIsPrimary()).isTrue();
        assertThat(capturedOAuth.getLinkedAt()).isEqualTo(fixedTime);
        assertThat(capturedOAuth.getLastUsed()).isEqualTo(fixedTime);
        assertThat(capturedOAuth.getUser()).isEqualTo(savedUser);
        assertThat(capturedOAuth.getProviderName()).isEqualTo("google");
        assertThat(capturedOAuth.getProviderUserId()).isEqualTo("google123");
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        String email = "[email protected]";
        UserEntity user = new UserEntity();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findByEmail(email);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        String email = "[email protected]";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.findByEmail(email);
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        Long id = 1L;
        UserEntity user = new UserEntity();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void getFullName_shouldConcatenateFirstAndLastName() {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");

        String result = userService.getFullName(user);

        assertThat(result).isEqualTo("John Doe");
    }

    @Test
    void getFullName_shouldHandleEmptyLastName() {
        UserEntity user = new UserEntity();
        user.setFirstName("Madonna");
        user.setLastName("");

        String result = userService.getFullName(user);

        assertThat(result).isEqualTo("Madonna ");
    }

    @Test
    void findByOAuthProvider_shouldReturnUserWhenExists() {
        String providerName = "google";
        String providerUserId = "google123";

        UserEntity user = new UserEntity();
        UserOAuthProvider oauthProvider = new UserOAuthProvider();
        oauthProvider.setUser(user);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.of(oauthProvider));

        Optional<UserEntity> result = userService.findByOAuthProvider(providerName, providerUserId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    private UserEntity invokeCreateNewUserFromOAuth() {
        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(any(), any()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(
                        invocation -> {
                            UserEntity userToSave = invocation.getArgument(0);
                            savedUser.setEmail(userToSave.getEmail());
                            savedUser.setFirstName(userToSave.getFirstName());
                            savedUser.setLastName(userToSave.getLastName());
                            savedUser.setProfilePictureUrl(userToSave.getProfilePictureUrl());
                            return savedUser;
                        });

        return userService.createOrUpdateUserFromOAuth(oAuth2User, "google");
    }

    private OAuth2User createMockOAuth2User() {
        return createMockOAuth2User(
                "[email protected]", "John Doe", "google123", "https://pic.com/pic.jpg");
    }

    private OAuth2User createMockOAuth2User(String email, String name, String sub, String picture) {
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("email")).thenReturn(email);
        when(mockUser.getAttribute("name")).thenReturn(name);
        when(mockUser.getAttribute("sub")).thenReturn(sub);
        when(mockUser.getAttribute("picture")).thenReturn(picture);
        return mockUser;
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldUpdateExistingUserWithNullName() {
        String email = "[email protected]";
        String providerUserId = "google123";
        String providerName = "google";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        UserOAuthProvider existingOAuth = new UserOAuthProvider();
        existingOAuth.setUser(existingUser);
        existingOAuth.setProviderName(providerName);
        existingOAuth.setProviderUserId(providerUserId);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn("https://new-pic.com/pic.jpg");

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.of(existingOAuth));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result).isEqualTo(existingUser);
        assertThat(existingUser.getFirstName()).isEqualTo("John");
        assertThat(existingUser.getLastName()).isEqualTo("Doe");
        assertThat(existingUser.getProfilePictureUrl()).isEqualTo("https://new-pic.com/pic.jpg");

        verify(userRepository).save(existingUser);
        verify(oauthProviderRepository).save(existingOAuth);
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldUpdateExistingUserWithNullProfilePicture() {
        String email = "[email protected]";
        String name = "John Updated";
        String providerUserId = "google123";
        String providerName = "google";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setProfilePictureUrl("https://old-pic.com/pic.jpg");

        UserOAuthProvider existingOAuth = new UserOAuthProvider();
        existingOAuth.setUser(existingUser);
        existingOAuth.setProviderName(providerName);
        existingOAuth.setProviderUserId(providerUserId);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.of(existingOAuth));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result).isEqualTo(existingUser);
        assertThat(existingUser.getFirstName()).isEqualTo("John");
        assertThat(existingUser.getLastName()).isEqualTo("Updated");
        assertThat(existingUser.getProfilePictureUrl()).isEqualTo("https://old-pic.com/pic.jpg");

        verify(userRepository).save(existingUser);
        verify(oauthProviderRepository).save(existingOAuth);
    }

    @Test
    void createOrUpdateUserFromOAuth_shouldUpdateExistingUserWithBothNullValues() {
        String email = "[email protected]";
        String providerUserId = "google123";
        String providerName = "google";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setProfilePictureUrl("https://old-pic.com/pic.jpg");

        UserOAuthProvider existingOAuth = new UserOAuthProvider();
        existingOAuth.setUser(existingUser);
        existingOAuth.setProviderName(providerName);
        existingOAuth.setProviderUserId(providerUserId);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.of(existingOAuth));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result).isEqualTo(existingUser);
        assertThat(existingUser.getFirstName()).isEqualTo("John");
        assertThat(existingUser.getLastName()).isEqualTo("Doe");
        assertThat(existingUser.getProfilePictureUrl()).isEqualTo("https://old-pic.com/pic.jpg");

        verify(userRepository).save(existingUser);
        verify(oauthProviderRepository).save(existingOAuth);
    }

    @Test
    void linkOAuthProvider_shouldSetPrimaryFalseForSecondProvider() {
        String email = "[email protected]";
        String name = "John Doe";
        String providerUserId = "github456";
        String providerName = "github";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        UserOAuthProvider existingProvider = new UserOAuthProvider();
        existingProvider.setProviderName("google");
        existingProvider.setProviderUserId("google123");
        existingProvider.setIsPrimary(true);
        existingUser.addOAuthProvider(existingProvider);

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        ArgumentCaptor<UserOAuthProvider> oauthCaptor =
                ArgumentCaptor.forClass(UserOAuthProvider.class);
        verify(oauthProviderRepository).save(oauthCaptor.capture());
        UserOAuthProvider capturedOAuth = oauthCaptor.getValue();

        assertThat(capturedOAuth.getIsPrimary()).isFalse();
        assertThat(capturedOAuth.getUser()).isEqualTo(existingUser);
        assertThat(capturedOAuth.getProviderName()).isEqualTo(providerName);
        assertThat(capturedOAuth.getProviderUserId()).isEqualTo(providerUserId);

        assertThat(existingUser.getOauthProviders()).hasSize(2);
    }

    @Test
    void linkOAuthProvider_shouldHandleExistingUserWithNullName() {
        String email = "[email protected]";
        String providerUserId = "github789";
        String providerName = "github";

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Smith");

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("sub")).thenReturn(providerUserId);
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        UserEntity result = userService.createOrUpdateUserFromOAuth(oAuth2User, providerName);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");

        ArgumentCaptor<UserOAuthProvider> oauthCaptor =
                ArgumentCaptor.forClass(UserOAuthProvider.class);
        verify(oauthProviderRepository).save(oauthCaptor.capture());
        UserOAuthProvider capturedOAuth = oauthCaptor.getValue();

        assertThat(capturedOAuth.getUser()).isEqualTo(existingUser);
        assertThat(capturedOAuth.getProviderName()).isEqualTo(providerName);
        assertThat(capturedOAuth.getProviderUserId()).isEqualTo(providerUserId);
    }
}
