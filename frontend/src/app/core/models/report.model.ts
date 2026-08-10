export type ReportType = 'EXECUTIVE' | 'PORTFOLIO' | 'PROJECT' | 'KPI' | 'RISK';
export type ReportFormat = 'PDF' | 'EXCEL';
export type ReportStatus = 'COMPLETED' | 'FAILED';
export type ReportScopeType = 'WORKSPACE' | 'PORTFOLIO' | 'PROJECT';

export interface GenerateReportRequest {
  readonly reportType: ReportType;
  readonly format: ReportFormat;
  readonly scopeId?: string | null;
  readonly scopeType?: ReportScopeType | null;
}

export interface ReportSummary {
  readonly id: string;
  readonly title: string;
  readonly reportType: ReportType;
  readonly format: ReportFormat;
  readonly status: ReportStatus;
  readonly scopeType: ReportScopeType | null;
  readonly scopeId: string | null;
  readonly generatedBy: string;
  readonly fileName: string | null;
  readonly contentType: string | null;
  readonly fileSizeBytes: number | null;
  readonly errorMessage: string | null;
  readonly generatedAt: string;
  readonly createdAt: string;
}
