-- Sibyl report-book v2: weekly review workflow. Keeps legacy V7 tables untouched.
CREATE TABLE sibyl_report_profiles (
 username VARCHAR(100) NOT NULL PRIMARY KEY,
 trainee_enabled BOOLEAN NOT NULL DEFAULT FALSE,
 trainer_enabled BOOLEAN NOT NULL DEFAULT FALSE,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 source VARCHAR(12) NOT NULL DEFAULT 'MANUAL',
 last_synced_at TIMESTAMP NULL,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sibyl_report_assignments (
 trainee_username VARCHAR(100) NOT NULL PRIMARY KEY,
 trainer_username VARCHAR(100) NOT NULL,
 assigned_by VARCHAR(100) NOT NULL,
 assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_sibyl_report_trainer(trainer_username),
 CONSTRAINT fk_report_trainee FOREIGN KEY (trainee_username) REFERENCES sibyl_report_profiles(username),
 CONSTRAINT fk_report_trainer FOREIGN KEY (trainer_username) REFERENCES sibyl_report_profiles(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sibyl_report_weeks (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 owner_username VARCHAR(100) NOT NULL,
 week_start DATE NOT NULL,
 report_mode VARCHAR(8) NOT NULL DEFAULT 'DAILY',
 weekly_summary TEXT NULL,
 status VARCHAR(12) NOT NULL DEFAULT 'DRAFT',
 reviewer_username VARCHAR(100) NULL,
 review_feedback VARCHAR(1500) NULL,
 submitted_at TIMESTAMP NULL,
 reviewed_at TIMESTAMP NULL,
 revision BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uq_report_week(owner_username,week_start),
 INDEX idx_report_pending(status,week_start),
 INDEX idx_report_owner(owner_username,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sibyl_report_days (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 week_id BIGINT NOT NULL,
 day_date DATE NOT NULL,
 location_type VARCHAR(16) NOT NULL DEFAULT 'BETRIEB',
 attendance VARCHAR(12) NOT NULL DEFAULT 'ANWESEND',
 minutes_spent INT NOT NULL DEFAULT 0,
 activity_text TEXT NOT NULL,
 UNIQUE KEY uq_report_day(week_id,day_date),
 CONSTRAINT fk_report_week_day FOREIGN KEY(week_id) REFERENCES sibyl_report_weeks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sibyl_report_events (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 week_id BIGINT NOT NULL,
 actor_username VARCHAR(100) NOT NULL,
 event_type VARCHAR(24) NOT NULL,
 note VARCHAR(1500) NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_report_history(week_id,created_at),
 CONSTRAINT fk_report_week_event FOREIGN KEY(week_id) REFERENCES sibyl_report_weeks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- The report book works with local users as well; AD discovery is optional.
DELETE FROM sibyl_addon_dependencies WHERE addon_id='Sibyl.report-book' AND requires_addon_id='Sibyl.ad';
