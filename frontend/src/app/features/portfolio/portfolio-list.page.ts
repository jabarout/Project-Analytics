import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PortfolioApiService } from '../../core/services/portfolio-api.service';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { PortfolioProjectSummary, PortfolioSummary } from '../../core/models/portfolio.model';
import { Workspace } from '../../core/models/workspace.model';
import { ExplorerProjectRow } from '../../core/models/explorer.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ProjectMembershipPickerComponent } from '../../shared/components/portfolio/project-membership-picker.component';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

@Component({
  selector: 'app-portfolio-list-page',
  standalone: true,
  imports: [
    PaRevealDirective,
    RouterLink,
    ReactiveFormsModule,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    ProjectMembershipPickerComponent,
  ],
  templateUrl: './portfolio-list.page.html',
  styleUrl: './portfolio-list.page.scss',
})
export class PortfolioListPage implements OnInit {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly formBuilder = inject(FormBuilder);

  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly portfolios = signal<PortfolioSummary[]>([]);
  readonly workspaces = signal<Workspace[]>([]);
  readonly workspaceProjects = signal<readonly PortfolioProjectSummary[]>([]);
  readonly analyticsRows = signal<readonly ExplorerProjectRow[]>([]);
  readonly selectedProjectIds = signal<string[]>([]);

  readonly form = this.formBuilder.nonNullable.group({
    workspaceId: ['', Validators.required],
    name: ['', [Validators.required, Validators.maxLength(200)]],
    description: [''],
  });

  ngOnInit(): void {
    this.workspaceApi.listWorkspaces().subscribe({
      next: (items) => {
        this.workspaces.set(items);
        if (items.length > 0) {
          this.form.patchValue({ workspaceId: items[0].id });
          this.loadWorkspaceProjects(items[0].id);
        }
      },
    });
    this.form.controls.workspaceId.valueChanges.subscribe((workspaceId) => {
      this.selectedProjectIds.set([]);
      if (workspaceId) {
        this.loadWorkspaceProjects(workspaceId);
      } else {
        this.workspaceProjects.set([]);
      }
    });
    this.reload();
  }

  onSelectionChange(ids: string[]): void {
    this.selectedProjectIds.set(ids);
  }

  reload(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.portfolioApi.listPortfolios().subscribe({
      next: (items) => {
        this.portfolios.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load portfolios from local data.');
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
    this.creating.set(true);
    const raw = this.form.getRawValue();
    const projectIds = this.selectedProjectIds();
    this.portfolioApi
      .createPortfolio({
        workspaceId: raw.workspaceId,
        name: raw.name,
        description: raw.description || null,
        projectIds,
      })
      .subscribe({
        next: (created) => {
          this.creating.set(false);
          this.message.set(
            projectIds.length > 0
              ? `Portfolio “${created.name}” created with ${projectIds.length} project(s).`
              : `Portfolio “${created.name}” created. You can add projects anytime.`
          );
          this.form.patchValue({ name: '', description: '' });
          this.selectedProjectIds.set([]);
          this.reload();
        },
        error: (error: { error?: { error?: { message?: string } } }) => {
          this.creating.set(false);
          this.errorMessage.set(error?.error?.error?.message ?? 'Unable to create portfolio.');
        },
      });
  }

  private loadWorkspaceProjects(workspaceId: string): void {
    this.portfolioApi.listWorkspaceProjects(workspaceId).subscribe({
      next: (projects) => this.workspaceProjects.set(projects),
      error: () => this.workspaceProjects.set([]),
    });
    this.analyticsApi.getExplorerProjects(workspaceId).subscribe({
      next: (rows) => this.analyticsRows.set(rows),
      error: () => this.analyticsRows.set([]),
    });
  }
}
