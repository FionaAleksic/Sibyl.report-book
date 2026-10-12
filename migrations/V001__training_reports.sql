CREATE TABLE training_reports (
 id CHAR(36) NOT NULL PRIMARY KEY,
 trainee_id VARCHAR(128) NOT NULL,
 week_start DATE NOT NULL,
 report_body LONGTEXT NOT NULL,
 state VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
 created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 INDEX idx_training_week (trainee_id, week_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
