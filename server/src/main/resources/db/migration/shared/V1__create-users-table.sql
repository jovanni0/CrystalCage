CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE "user"
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(64) NOT NULL,
    role            VARCHAR(255) NOT NULL DEFAULT  'user',
    created_at      DATE NOT NULL DEFAULT CURRENT_DATE
);
