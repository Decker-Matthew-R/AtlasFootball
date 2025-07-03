CREATE TABLE user_oauth_providers
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider_name     VARCHAR(50)  NOT NULL,
    provider_user_id  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255),
    provider_username VARCHAR(255),
    is_primary        BOOLEAN               DEFAULT false,
    linked_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (provider_name, provider_user_id)
);
