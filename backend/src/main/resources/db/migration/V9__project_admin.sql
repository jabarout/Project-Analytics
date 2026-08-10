-- M11B: project admin from OpenProject memberships (Project admin role)
ALTER TABLE project ADD COLUMN admin_name VARCHAR(500);

COMMENT ON COLUMN project.admin_name IS 'Display name(s) of OpenProject project admins (from memberships), not WP assignees.';
