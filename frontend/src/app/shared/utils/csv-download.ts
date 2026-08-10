/**
 * Client-side CSV download helper (presentation/export only).
 */
export function downloadCsv(filename: string, csvBody: string): void {
  const blob = new Blob([csvBody], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

export function toCsvRow(values: Array<string | number | null | undefined>): string {
  return values
    .map((value) => {
      const raw = value == null ? '' : String(value);
      if (raw.includes(',') || raw.includes('"') || raw.includes('\n')) {
        return `"${raw.replaceAll('"', '""')}"`;
      }
      return raw;
    })
    .join(',');
}
