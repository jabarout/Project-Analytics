import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ReportApiService } from '../../core/services/report-api.service';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { PortfolioApiService } from '../../core/services/portfolio-api.service';
import {
  ReportFormat,
  ReportScopeType,
  ReportSummary,
  ReportType,
} from '../../core/models/report.model';
import { Workspace } from '../../core/models/workspace.model';
import { PortfolioSummary } from '../../core/models/portfolio.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

/**
 * Report generation and history. Presentation only — backend owns assembly and export.
 */
@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [
    PaRevealDirective,ReactiveFormsModule, LoadingSpinnerComponent, EmptyStateComponent, DatePipe],
  templateUrl: './reports.page.html',
  styleUrl: './reports.page.scss',
})
export class ReportsPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly reportApi = inject(ReportApiService);
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly generating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly history = signal<ReportSummary[]>([]);
  readonly workspaces = signal<Workspace[]>([]);
  readonly portfolios = signal<PortfolioSummary[]>([]);
  readonly selectedReportType = signal<ReportType>('EXECUTIVE');
  readonly selectedScopeType = signal<ReportScopeType>('WORKSPACE');

  readonly form = this.formBuilder.nonNullable.group({
    reportType: ['EXECUTIVE' as ReportType, Validators.required],
    format: ['PDF' as ReportFormat, Validators.required],
    scopeType: ['WORKSPACE' as ReportScopeType],
    scopeId: [''],
  });

  needsScope(): boolean {
    return this.selectedReportType() !== 'EXECUTIVE';
  }

  showScopeType(): boolean {
    const type = this.selectedReportType();
    return type === 'KPI' || type === 'RISK';
  }

  ngOnInit(): void {
    this.reloadHistory();
    this.workspaceApi.listWorkspaces().subscribe({
      next: (items) => this.workspaces.set(items),
      error: () => this.errorMessage.set('Unable to load workspaces for report scope.'),
    });
    this.portfolioApi.listPortfolios().subscribe({
      next: (items) => this.portfolios.set(items),
      error: () => {
        /* optional for executive-only use */
      },
    });

    const q = this.route.snapshot.queryParamMap;
    const reportType = q.get('reportType') as ReportType | null;
    const scopeType = q.get('scopeType') as ReportScopeType | null;
    const scopeId = q.get('scopeId');
    if (reportType) {
      this.form.controls.reportType.setValue(reportType);
      this.selectedReportType.set(reportType);
    }
    if (scopeType) {
      this.form.controls.scopeType.setValue(scopeType);
      this.selectedScopeType.set(scopeType);
    }
    if (scopeId) {
      this.form.controls.scopeId.setValue(scopeId);
    }

    this.form.controls.reportType.valueChanges.subscribe((type) => {
      this.selectedReportType.set(type);
      this.form.controls.scopeId.setValue('');
      this.successMessage.set(null);
      this.errorMessage.set(null);
    });
    this.form.controls.scopeType.valueChanges.subscribe((scopeTypeValue) => {
      this.selectedScopeType.set(scopeTypeValue);
      this.form.controls.scopeId.setValue('');
    });
  }

  reloadHistory(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.reportApi.listHistory().subscribe({
      next: (items) => {
        this.history.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load report history.');
      },
    });
  }

  generate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const needsScope = raw.reportType !== 'EXECUTIVE';
    if (needsScope && !raw.scopeId) {
      this.errorMessage.set('Select a scope (workspace, portfolio, or project id) for this report type.');
      return;
    }

    let scopeType: ReportScopeType | null = null;
    if (raw.reportType === 'PORTFOLIO') {
      scopeType = 'PORTFOLIO';
    } else if (raw.reportType === 'PROJECT') {
      scopeType = 'PROJECT';
    } else if (raw.reportType === 'KPI' || raw.reportType === 'RISK') {
      scopeType = raw.scopeType;
    }

    this.generating.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.reportApi
      .generate({
        reportType: raw.reportType,
        format: raw.format,
        scopeId: needsScope ? raw.scopeId : null,
        scopeType,
      })
      .subscribe({
        next: (report) => {
          this.generating.set(false);
          this.successMessage.set(`Generated “${report.title}” (${report.format}).`);
          this.reloadHistory();
        },
        error: () => {
          this.generating.set(false);
          this.errorMessage.set(
            'Report generation failed. Ensure the selected scope has synchronized analytics data.'
          );
        },
      });
  }

  download(report: ReportSummary): void {
    if (report.status !== 'COMPLETED') {
      return;
    }
    this.reportApi.download(report.id, report.fileName ?? 'report');
  }

  formatBytes(bytes: number | null): string {
    if (bytes == null) {
      return '—';
    }
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    return `${(bytes / 1024).toFixed(1)} KB`;
  }
}
