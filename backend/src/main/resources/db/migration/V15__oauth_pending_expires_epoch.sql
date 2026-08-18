-- =============================================================================
-- Store OAuth pending expiry as UTC epoch seconds (avoids H2/Postgres Instant TZ quirks).
-- =============================================================================

ALTER TABLE oauth_connect_pending
    ADD COLUMN expires_at_epoch BIGINT;

UPDATE oauth_connect_pending
SET expires_at_epoch = EXTRACT(EPOCH FROM expires_at)::BIGINT
WHERE expires_at_epoch IS NULL;

ALTER TABLE oauth_connect_pending
    ALTER COLUMN expires_at_epoch SET NOT NULL;

DROP INDEX IF EXISTS idx_oauth_connect_pending_expires;

ALTER TABLE oauth_connect_pending
    DROP COLUMN expires_at;

CREATE INDEX idx_oauth_connect_pending_expires ON oauth_connect_pending (expires_at_epoch);
