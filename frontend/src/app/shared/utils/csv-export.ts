import { ExplorerColumnId, ExplorerProjectRow } from '../../core/models/explorer.model';

const HEADERS: Record<ExplorerColumnId, string> = {
  name: 'Project',
  status: 'Status',
  progress: 'Actual progress',
  expectedProgress: 'Expected progress',
  progressGap: 'Progress gap',
  healthScore: 'Health',
  riskScore: 'Risk',
  attentionScore: 'Needs Attention',
  delayed: 'Delayed',
  overdueWorkPackageCount: 'Overdue WPs',
  endDate: 'Project finish',
  nextDeadline: 'Next deadline',
  projectAdmin: 'Project admin',
  portfolioNames: 'Portfolios',
  recommendations: 'Recommendations',
};

function cell(row: ExplorerProjectRow, col: ExplorerColumnId): string {
  switch (col) {
    case 'name':
      return row.name;
    case 'status':
      return row.status ?? '';
    case 'progress':
      return row.progress == null ? '' : String(row.progress);
    case 'expectedProgress':
      return row.expectedProgress == null ? '' : String(row.expectedProgress);
    case 'progressGap':
      return row.progressGap == null ? '' : String(row.progressGap);
    case 'healthScore':
      return row.healthScore == null ? '' : String(row.healthScore);
    case 'riskScore':
      return row.riskScore == null ? '' : String(row.riskScore);
    case 'attentionScore':
      return row.attentionScore == null ? '' : String(row.attentionScore);
    case 'delayed':
      return row.delayed ? 'Yes' : 'No';
    case 'overdueWorkPackageCount':
      return String(row.overdueWorkPackageCount ?? 0);
    case 'endDate':
      return row.endDate ?? '';
    case 'nextDeadline':
      return row.nextDeadline ?? '';
    case 'projectAdmin':
      return row.projectAdmin ?? '';
    case 'portfolioNames':
      return row.portfolioNames.join('; ');
    default:
      return '';
  }
}

function escapeCsv(value: string): string {
  if (/[",\n]/.test(value)) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

/** Client-side CSV of the current Explorer result set (visible columns). */
export function downloadExplorerCsv(
  rows: readonly ExplorerProjectRow[],
  columns: readonly ExplorerColumnId[],
  filename = 'explorer-export.csv'
): void {
  const cols = columns.filter((c) => c !== 'recommendations');
  const header = cols.map((c) => escapeCsv(HEADERS[c] ?? c)).join(',');
  const lines = rows.map((row) => cols.map((c) => escapeCsv(cell(row, c))).join(','));
  const blob = new Blob([[header, ...lines].join('\n')], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}
