-- Existing installations: execute once before starting this version. No existing templates become public.
ALTER TABLE prompt ADD COLUMN is_public TINYINT NOT NULL DEFAULT 0;
ALTER TABLE prompt ADD COLUMN share_version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_prompt_public ON prompt(is_public, delete_status, update_time);
CREATE INDEX idx_prompt_owner ON prompt(user_id, delete_status, update_time);
