ALTER TABLE admin_user
  ADD COLUMN session_token_hash VARCHAR(128);

ALTER TABLE admin_user
  ADD COLUMN session_issued_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE admin_user
  ADD COLUMN session_expires_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE admin_user
  ADD CONSTRAINT uk_admin_user_session_token_hash UNIQUE (session_token_hash);
