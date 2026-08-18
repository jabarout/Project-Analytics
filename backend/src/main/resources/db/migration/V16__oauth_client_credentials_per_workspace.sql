-- =============================================================================
-- Multi-OpenProject OAuth: per-workspace OAuth client application credentials
-- Client secret encrypted at rest. Pending rows hold credentials for the redirect.
-- =============================================================================

ALTER TABLE workspace_credential
    ADD COLUMN oauth_client_id VARCHAR(200);

ALTER TABLE workspace_credential
    ADD COLUMN oauth_client_secret_ciphertext TEXT;

ALTER TABLE oauth_connect_pending
    ADD COLUMN oauth_client_id VARCHAR(200);

ALTER TABLE oauth_connect_pending
    ADD COLUMN oauth_client_secret_ciphertext TEXT;

UPDATE oauth_connect_pending
SET oauth_client_id = ''
WHERE oauth_client_id IS NULL;

UPDATE oauth_connect_pending
SET oauth_client_secret_ciphertext = ''
WHERE oauth_client_secret_ciphertext IS NULL;
