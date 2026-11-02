DROP TABLE IF EXISTS prompt;
CREATE TABLE prompt (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL,
 title VARCHAR(100), content TEXT, category VARCHAR(50), model_type VARCHAR(50), tags VARCHAR(255),
 is_favorite INT DEFAULT 0, use_count INT DEFAULT 0, is_public INT DEFAULT 0,
 share_version BIGINT NOT NULL DEFAULT 0, create_time TIMESTAMP, update_time TIMESTAMP, delete_status INT DEFAULT 0
);
