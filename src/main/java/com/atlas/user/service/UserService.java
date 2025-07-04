package com.atlas.user.service;

import com.atlas.user.repository.UserOAuthProviderRepository;
import com.atlas.user.repository.UserRepository;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.repository.model.UserOAuthProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserOAuthProviderRepository oauthProviderRepository;

    public UserEntity createOrUpdateUserFromOAuth(OAuth2User oAuth2User, String providerName) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerUserId = oAuth2User.getAttribute("sub");
        String profilePicture = oAuth2User.getAttribute("picture");

        Optional<UserOAuthProvider> existingOAuth =
                oauthProviderRepository.findByProviderNameAndProviderUserIdWithUser(
                        providerName, providerUserId);

        if (existingOAuth.isPresent()) {
            UserEntity user = existingOAuth.get().getUser();
            updateUserFromOAuth(user, oAuth2User);
            updateOAuthProviderLastUsed(existingOAuth.get());
            return userRepository.save(user);
        }

        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        UserEntity user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            updateUserFromOAuth(user, oAuth2User);

        } else {
            user = createNewUserFromOAuth(oAuth2User);
            user = userRepository.save(user);
        }

        linkOAuthProvider(user, providerName, providerUserId, email);

        return user;
    }

    private UserEntity createNewUserFromOAuth(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String profilePicture = oAuth2User.getAttribute("picture");

        String[] nameParts = parseName(name);
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePictureUrl(profilePicture);
        user.updateLastLogin();

        return user;
    }

    private void updateUserFromOAuth(UserEntity user, OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        String profilePicture = oAuth2User.getAttribute("picture");

        if (name != null) {
            String[] nameParts = parseName(name);
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts[1]);
        }

        if (profilePicture != null) {
            user.setProfilePictureUrl(profilePicture);
        }

        user.updateLastLogin();
    }

    private void updateOAuthProviderLastUsed(UserOAuthProvider oauthProvider) {
        oauthProvider.setLastUsed(LocalDateTime.now());
        oauthProviderRepository.save(oauthProvider);
    }

    private String[] parseName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[] {"Unknown", ""};
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        return new String[] {firstName, lastName};
    }

    private void linkOAuthProvider(
            UserEntity user, String providerName, String providerUserId, String providerEmail) {
        UserOAuthProvider oauthProvider = new UserOAuthProvider();
        oauthProvider.setUser(user);
        oauthProvider.setProviderName(providerName);
        oauthProvider.setProviderUserId(providerUserId);
        oauthProvider.setProviderEmail(providerEmail);
        oauthProvider.setLinkedAt(LocalDateTime.now());
        oauthProvider.setLastUsed(LocalDateTime.now());

        if (user.getOauthProviders().isEmpty()) {
            oauthProvider.setIsPrimary(true);
        }

        user.addOAuthProvider(oauthProvider);
        oauthProviderRepository.save(oauthProvider);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public String getFullName(UserEntity user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByOAuthProvider(String providerName, String providerUserId) {
        return oauthProviderRepository
                .findByProviderNameAndProviderUserIdWithUser(providerName, providerUserId)
                .map(UserOAuthProvider::getUser);
    }
}
