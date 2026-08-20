-- =============================================================================
-- Dark-first UI default for new preferences.
-- Existing rows keep their saved theme unless still on the historical default.
-- Seed admin is switched to dark for the product default experience.
-- =============================================================================

ALTER TABLE user_preference
    ALTER COLUMN theme SET DEFAULT 'dark';

UPDATE user_preference up
SET theme = 'dark',
    updated_at = CURRENT_TIMESTAMP
FROM users u
WHERE up.user_id = u.id
  AND u.username = 'admin'
  AND lower(up.theme) = 'light';

COMMENT ON COLUMN user_preference.theme IS 'UI theme (light|dark). Product default is dark.';
