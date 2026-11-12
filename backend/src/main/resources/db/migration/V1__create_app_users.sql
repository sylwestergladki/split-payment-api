CREATE TABLE app_users (
                           id UUID PRIMARY KEY,
                           keycloak_subject VARCHAR(255) NOT NULL,
                           email VARCHAR(320) NOT NULL,
                           display_name VARCHAR(100) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL,
                           updated_at TIMESTAMPTZ NOT NULL,

                           CONSTRAINT uk_app_users_keycloak_subject
                               UNIQUE (keycloak_subject)
);

