CREATE TABLE author(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  VARCHAR(255),
    last_name   VARCHAR(255) NOT NULL
);
