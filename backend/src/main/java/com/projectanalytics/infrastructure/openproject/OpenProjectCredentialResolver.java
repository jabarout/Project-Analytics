package com.projectanalytics.infrastructure.openproject;

import java.util.UUID;

/**
 * Resolves OpenProject connection credentials for a workspace.
 *
 * <p>The synchronization engine depends only on this port. The default implementation uses the
 * environment API key. A future OAuth 2.0 implementation can replace or decorate this resolver
 * without changing import, history, or incremental sync logic.
 */
public interface OpenProjectCredentialResolver {

    /**
     * @param workspaceId  workspace being synchronized
     * @param workspaceBaseUrl base URL stored on the workspace
     */
    OpenProjectConnectionProperties resolve(UUID workspaceId, String workspaceBaseUrl);
}
