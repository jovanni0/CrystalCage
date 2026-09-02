CREATE TABLE "invite_code"
(
    code        CHAR(8) PRIMARY KEY,
    created_by  UUID,
    used_by     UUID UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ,
    used_at     TIMESTAMPTZ,

    CONSTRAINT code_creator_id FOREIGN KEY (created_by) REFERENCES "user"(id) ON DELETE SET NULL,
    CONSTRAINT code_user_id FOREIGN KEY (used_by) REFERENCES "user"(id) ON DELETE CASCADE
);