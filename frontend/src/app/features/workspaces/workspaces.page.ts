import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { SynchronizationStatus, Workspace, WorkspaceMember } from '../../core/models/workspace.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

/**
 * Connections: OpenProject OAuth (preferred) / API key / sync / disconnect + M15 access grants.
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
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly connecting = signal(false);
  readonly oauthConnecting = signal(false);
  readonly oauthEnabled = signal(false);
  readonly oauthRedirectUri = signal<string | null>(null);
  readonly globalClientDefaultsAvailable = signal(false);
  /** Set after startOAuth; user opens OP in a new window via an explicit click (avoids blank-popup CSRF issues). */
  readonly pendingAuthorizationUrl = signal<string | null>(null);
  readonly showApiKeyForm = signal(false);
  readonly syncingId = signal<string | null>(null);
  readonly renamingId = signal<string | null>(null);
  readonly grantingId = signal<string | null>(null);
  readonly revokingUserId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);
  readonly latestSync = signal<Record<string, SynchronizationStatus>>({});
  readonly editName = signal<Record<string, string>>({});
  readonly members = signal<Record<string, WorkspaceMember[]>>({});
  readonly grantEmail = signal<Record<string, string>>({});

  readonly isMulti = computed(() => this.workspaces().length > 1);
  readonly single = computed(() => (this.workspaces().length === 1 ? this.workspaces()[0] : null));

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.maxLength(200)]],
    baseUrl: ['', [Validators.required, Validators.maxLength(500)]],
    clientId: ['', [Validators.maxLength(200)]],
    clientSecret: ['', [Validators.maxLength(500)]],
    apiKey: ['', [Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    this.listenForOAuthPopupResult();
    this.applyOAuthQueryFeedback();
    this.workspaceApi.oauthStatus().subscribe({
      next: (status) => {
        this.oauthEnabled.set(!!status.enabled);
        this.oauthRedirectUri.set(status.redirectUri ?? null);
        this.globalClientDefaultsAvailable.set(!!status.globalClientDefaultsAvailable);
        // Prefer OAuth when PA redirect URI is configured; keep API key as alternative.
        this.showApiKeyForm.set(!status.enabled);
      },
      error: () => {
        this.oauthEnabled.set(false);
        this.showApiKeyForm.set(true);
      },
    });
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
          if (workspace.workspaceAdmin) {
            this.reloadMembers(workspace.id);
          }
        });
        this.editName.set(names);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load connections.');
      },
    });
  }

  connectWithOAuth(): void {
    const baseUrlControl = this.form.controls.baseUrl;
    if (baseUrlControl.invalid) {
      baseUrlControl.markAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const clientId = raw.clientId.trim();
    const clientSecret = raw.clientSecret.trim();
    if (!this.globalClientDefaultsAvailable() && (!clientId || !clientSecret)) {
      this.form.controls.clientId.markAsTouched();
      this.form.controls.clientSecret.markAsTouched();
      this.errorMessage.set(
        'Client ID and Client secret are required. Create an OAuth application in OpenProject first.'
      );
      return;
    }

    this.message.set(null);
    this.errorMessage.set(null);
    this.pendingAuthorizationUrl.set(null);
    this.oauthConnecting.set(true);
    this.workspaceApi
      .startOAuth({
        baseUrl: raw.baseUrl.trim(),
        name: raw.name.trim() || undefined,
        clientId: clientId || undefined,
        clientSecret: clientSecret || undefined,
      })
      .subscribe({
        next: (started) => {
          // Never keep the secret in the form after submit.
          this.form.patchValue({ clientSecret: '' });
          this.oauthConnecting.set(false);
          this.pendingAuthorizationUrl.set(started.authorizationUrl);
          this.message.set('OAuth ready — open OpenProject sign-in below.');
        },
        error: (error: { error?: { error?: { message?: string } } }) => {
          this.oauthConnecting.set(false);
          this.form.patchValue({ clientSecret: '' });
          this.errorMessage.set(
            error?.error?.error?.message ??
              'Unable to start OpenProject OAuth. Check URL and OAuth app client id/secret.'
          );
        },
      });
  }

  /**
   * Opens OpenProject in a new window from a direct user click (keeps PA here; avoids blank-popup CSRF issues).
   */
  openOpenProjectSignIn(): void {
    const url = this.pendingAuthorizationUrl();
    if (!url) {
      return;
    }
    const popup = window.open(url, 'pa-openproject-oauth');
    if (!popup) {
      this.errorMessage.set(
        'Pop-up blocked. Allow pop-ups for this site, or copy the OpenProject URL from a new attempt.'
      );
      return;
    }
    this.message.set('OpenProject opened in another window. If you see a 422/CSRF error, refresh this page.');
    this.watchOAuthPopup(popup);
  }

  cancelPendingOAuth(): void {
    this.pendingAuthorizationUrl.set(null);
    this.message.set(null);
  }

  createWithApiKey(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.message.set(null);
    this.errorMessage.set(null);
    this.connecting.set(true);
    const raw = this.form.getRawValue();
    this.workspaceApi
      .connectWithApiKey({
        baseUrl: raw.baseUrl.trim(),
        name: raw.name.trim() || undefined,
        apiKey: raw.apiKey.trim() || undefined,
      })
      .subscribe({
        next: () => {
          this.connecting.set(false);
          this.form.reset({ name: '', baseUrl: '', clientId: '', clientSecret: '', apiKey: '' });
          this.message.set(
            'Connected with API key. OpenProject eligibility verified — you are Workspace Admin. Synchronize to load data.'
          );
          this.reload();
        },
        error: (error: { error?: { error?: { message?: string } } }) => {
          this.connecting.set(false);
          this.errorMessage.set(
            error?.error?.error?.message ??
              'Unable to connect. Check URL, API key, and OpenProject permissions (admin or Project admin).'
          );
        },
      });
  }

  toggleApiKeyForm(): void {
    this.showApiKeyForm.update((value) => !value);
  }

  setEditName(id: string, name: string): void {
    this.editName.update((current) => ({ ...current, [id]: name }));
  }

  setGrantEmail(id: string, email: string): void {
    this.grantEmail.update((current) => ({ ...current, [id]: email }));
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

  grantAccess(workspaceId: string): void {
    const email = (this.grantEmail()[workspaceId] ?? '').trim();
    if (!email) {
      return;
    }
    this.grantingId.set(workspaceId);
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.grantMember(workspaceId, email).subscribe({
      next: (member) => {
        this.grantingId.set(null);
        this.grantEmail.update((current) => ({ ...current, [workspaceId]: '' }));
        this.message.set(`Granted analytics access to ${member.email}.`);
        this.reloadMembers(workspaceId);
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.grantingId.set(null);
        this.errorMessage.set(error?.error?.error?.message ?? 'Unable to grant access.');
      },
    });
  }

  revokeAccess(workspaceId: string, member: WorkspaceMember): void {
    if (member.workspaceAdmin) {
      return;
    }
    const ok = confirm(`Revoke analytics access for ${member.email}?`);
    if (!ok) {
      return;
    }
    this.revokingUserId.set(member.userId);
    this.message.set(null);
    this.errorMessage.set(null);
    this.workspaceApi.revokeMember(workspaceId, member.userId).subscribe({
      next: () => {
        this.revokingUserId.set(null);
        this.message.set(`Revoked analytics access for ${member.email}.`);
        this.reloadMembers(workspaceId);
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.revokingUserId.set(null);
        this.errorMessage.set(error?.error?.error?.message ?? 'Unable to revoke access.');
      },
    });
  }

  private applyOAuthQueryFeedback(): void {
    const params = this.route.snapshot.queryParamMap;
    const oauth = params.get('oauth');
    if (oauth !== 'success' && oauth !== 'error') {
      return;
    }

    const message =
      oauth === 'success'
        ? 'Connected via OpenProject OAuth. Eligibility verified — you are Workspace Admin. Synchronize to load data.'
        : (params.get('message') ?? 'OpenProject OAuth connect failed.');

    // Popup completion path: notify the original PA window, then close this window.
    if (window.opener && !window.opener.closed) {
      try {
        window.opener.postMessage(
          {
            source: 'pa-openproject-oauth',
            oauth,
            message,
          },
          window.location.origin
        );
      } catch {
        // Cross-window messaging can fail; opener still polls popup close.
      }
      window.close();
      return;
    }

    if (oauth === 'success') {
      this.message.set(message);
      this.reload();
    } else {
      this.errorMessage.set(message);
    }
  }

  private listenForOAuthPopupResult(): void {
    window.addEventListener('message', (event: MessageEvent) => {
      if (event.origin !== window.location.origin) {
        return;
      }
      const data = event.data as { source?: string; oauth?: string; message?: string } | null;
      if (!data || data.source !== 'pa-openproject-oauth') {
        return;
      }
      this.oauthConnecting.set(false);
      this.pendingAuthorizationUrl.set(null);
      if (data.oauth === 'success') {
        this.message.set(
          data.message ??
            'Connected via OpenProject OAuth. Eligibility verified — you are Workspace Admin. Synchronize to load data.'
        );
        this.errorMessage.set(null);
        this.form.patchValue({ clientSecret: '' });
        this.reload();
      } else if (data.oauth === 'error') {
        this.errorMessage.set(data.message ?? 'OpenProject OAuth connect failed.');
      }
    });
  }

  private watchOAuthPopup(popup: Window): void {
    const timer = window.setInterval(() => {
      if (popup.closed) {
        window.clearInterval(timer);
        this.oauthConnecting.set(false);
        this.pendingAuthorizationUrl.set(null);
        // Refresh in case the popup closed after a successful connect without postMessage.
        this.reload();
      }
    }, 800);
  }

  private reloadMembers(workspaceId: string): void {
    this.workspaceApi.listMembers(workspaceId).subscribe({
      next: (items) => {
        this.members.update((current) => ({ ...current, [workspaceId]: items }));
      },
      error: () => {
        this.members.update((current) => ({ ...current, [workspaceId]: [] }));
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
