ALTER TABLE admin_user
  ADD COLUMN failed_login_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE admin_user
  ADD COLUMN login_locked_until TIMESTAMP WITH TIME ZONE;
