export interface Workspace {
  readonly id: string;
  readonly name: string;
  readonly baseUrl: string;
  readonly version: string | null;
  readonly synchronizationStatus: string;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface CreateWorkspaceRequest {
  readonly name: string;
  readonly baseUrl?: string;
}

export interface SynchronizationStatus {
  readonly historyId: string | null;
  readonly workspaceId: string;
  readonly syncType: string | null;
  readonly status: string;
  readonly synchronizedProjects: number;
  readonly synchronizedWorkPackages: number;
  readonly startedAt: string | null;
  readonly finishedAt: string | null;
  readonly durationMs: number | null;
  readonly errorMessage: string | null;
}
