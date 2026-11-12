CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    from_user_id UUID NOT NULL,
    to_user_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_settlements_group
        FOREIGN KEY (group_id) REFERENCES groups(id),

    CONSTRAINT fk_settlements_from_user
        FOREIGN KEY (from_user_id) REFERENCES app_users(id),

    CONSTRAINT fk_settlements_to_user
        FOREIGN KEY (to_user_id) REFERENCES app_users(id),

    CONSTRAINT chk_settlements_positive_amount
        CHECK (amount > 0),

    CONSTRAINT chk_settlements_different_users
        CHECK (from_user_id <> to_user_id)
);

CREATE INDEX idx_settlements_group_id
    ON settlements(group_id);