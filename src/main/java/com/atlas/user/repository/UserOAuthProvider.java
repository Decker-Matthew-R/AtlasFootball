package com.atlas.user.repository;

import com.atlas.user.repository.model.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(
        name = "user_oauth_providers",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"provider_name", "provider_user_id"})
        })
public class UserOAuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "provider_name", nullable = false)
    private String providerName; // "google", "github", "apple", etc.

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId; // OAuth provider's user ID (e.g., Google's "sub")

    @Column(name = "provider_email")
    private String providerEmail; // Email from OAuth provider

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    // Additional OAuth metadata (optional)
    @Column(name = "provider_username")
    private String providerUsername;

    @Column(name = "is_primary")
    private Boolean isPrimary = false; // If user has multiple OAuth providers

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (linkedAt == null) {
            linkedAt = LocalDateTime.now();
        }
        if (lastUsed == null) {
            lastUsed = LocalDateTime.now();
        }
    }
}
