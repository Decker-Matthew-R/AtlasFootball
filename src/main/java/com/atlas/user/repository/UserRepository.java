package com.atlas.user.repository;

import com.atlas.user.repository.model.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.oauthProviders WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithOAuthProviders(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE u.lastLogin < :cutoffDate")
    List<UserEntity> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :startDate")
    Long countNewUsersAfter(@Param("startDate") LocalDateTime startDate);
}
