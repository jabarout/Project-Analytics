import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { SynchronizationStatus, Workspace } from '../../core/models/workspace.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

/**
 * Connections: OpenProject connect / rename / sync / disconnect (M11B R6 + E2).
 */
@Component({
  selector: 'app-workspaces-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSpinnerComponent, EmptyStateComponent],
  templateUrl: './workspaces.page.html',
  styleUrl: './workspaces.page.scss',
})
export class WorkspacesPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly workspaceApi = inject(WorkspaceApiService);

  readonly loading = signal(true);
  readonly syncingId = signal<string | null>(null);
  readonly renamingId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);
  readonly latestSync = signal<Record<string, SynchronizationStatus>>({});
  readonly editName = signal<Record<string, string>>({});

  readonly isMulti = computed(() => this.workspaces().length > 1);
  readonly single = computed(() => (this.workspaces().length === 1 ? this.workspaces()[0] : null));

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    baseUrl: ['', [Validators.required, Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.workspaceApi.listWorkspaces().subscribe({
      next: (items) => {
        this.workspaces.set(items);
        this.loading.set(false);
        const names: Record<string, string> = {};
        items.forEach((workspace) => {
          names[workspace.id] = workspace.name;
          this.refreshStatus(workspace.id);
        });
        this.editName.set(names);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load connections.');
      },
    });
  }

  create(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.createWorkspace(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset({ name: '', baseUrl: '' });
        this.message.set('OpenProject connection registered.');
        this.reload();
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.errorMessage.set(error?.error?.error?.message ?? 'Unable to create connection.');
      },
    });
  }

  setEditName(id: string, name: string): void {
    this.editName.update((current) => ({ ...current, [id]: name }));
  }

  rename(workspaceId: string): void {
    const name = (this.editName()[workspaceId] ?? '').trim();
    if (!name) {
      return;
    }
    this.renamingId.set(workspaceId);
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.renameWorkspace(workspaceId, name).subscribe({
      next: () => {
        this.renamingId.set(null);
        this.message.set('Connection renamed.');
        this.reload();
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.renamingId.set(null);
        this.errorMessage.set(error?.error?.error?.message ?? 'Unable to rename connection.');
      },
    });
  }

  synchronize(workspaceId: string): void {
    this.syncingId.set(workspaceId);
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.synchronize(workspaceId).subscribe({
      next: (status) => {
        this.syncingId.set(null);
        this.latestSync.update((current) => ({ ...current, [workspaceId]: status }));
        this.message.set(
          status.status === 'SUCCESS'
            ? `Synchronization succeeded (${status.synchronizedProjects} projects, ${status.synchronizedWorkPackages} work packages).`
            : `Synchronization finished with status ${status.status}.`
        );
        this.reload();
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.syncingId.set(null);
        this.errorMessage.set(error?.error?.error?.message ?? 'Synchronization failed.');
        this.refreshStatus(workspaceId);
      },
    });
  }

  disconnect(workspace: Workspace): void {
    const ok = confirm(
      `Disconnect "${workspace.name}"?\n\nThis permanently deletes the local synchronized projects, analytics, portfolios, and sync history for this connection. OpenProject itself is not modified.`
    );
    if (!ok) {
      return;
    }
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.deleteWorkspace(workspace.id).subscribe({
      next: () => {
        this.message.set('Connection disconnected and local data removed.');
        this.reload();
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.errorMessage.set(error?.error?.error?.message ?? 'Unable to disconnect.');
      },
    });
  }

  private refreshStatus(workspaceId: string): void {
    this.workspaceApi.getSynchronizationStatus(workspaceId).subscribe({
      next: (status) => {
        this.latestSync.update((current) => ({ ...current, [workspaceId]: status }));
      },
    });
  }
}
