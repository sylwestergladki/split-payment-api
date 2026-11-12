CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    paid_by UUID NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_expenses_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX idx_expenses_group_id
    ON expenses(group_id);


CREATE TABLE expense_splits (
    expense_id UUID NOT NULL
        REFERENCES expenses(id) ON DELETE CASCADE,

    user_id UUID NOT NULL
        REFERENCES app_users(id),

    amount NUMERIC(19, 2) NOT NULL,

    PRIMARY KEY (expense_id, user_id),

    CONSTRAINT chk_expense_splits_amount_nonnegative
        CHECK (amount >= 0)
);

CREATE INDEX idx_expense_splits_user_id
    ON expense_splits(user_id);