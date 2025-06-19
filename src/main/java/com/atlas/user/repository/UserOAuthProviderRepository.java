package com.atlas.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOAuthProviderRepository extends JpaRepository<UserOAuthProvider, Long> {

    Optional<UserOAuthProvider> findByProviderNameAndProviderUserId(
            String providerName, String providerUserId);

    List<UserOAuthProvider> findByUserId(Long userId);

    List<UserOAuthProvider> findByProviderName(String providerName);

    @Query(
            "SELECT uop FROM UserOAuthProvider uop JOIN FETCH uop.user WHERE uop.providerName = :providerName AND uop.providerUserId = :providerUserId")
    Optional<UserOAuthProvider> findByProviderNameAndProviderUserIdWithUser(
            @Param("providerName") String providerName,
            @Param("providerUserId") String providerUserId);

    boolean existsByProviderNameAndProviderUserId(String providerName, String providerUserId);

    @Query("SELECT uop FROM UserOAuthProvider uop WHERE uop.user.email = :email")
    List<UserOAuthProvider> findByUserEmail(@Param("email") String email);
}
