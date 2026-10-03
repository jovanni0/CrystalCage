CREATE TABLE tag_category (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE
);

ALTER TABLE "tag"
    ADD COLUMN category_id UUID NULL DEFAULT NULL
    CONSTRAINT tag_category_key REFERENCES tag_category(id) ON DELETE SET NULL;