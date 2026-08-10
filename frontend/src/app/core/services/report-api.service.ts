import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { GenerateReportRequest, ReportSummary } from '../models/report.model';
import { ConfigurationService } from './configuration.service';

/**
 * Formal reporting API client (PDF/Excel). No client-side scoring.
 */
@Injectable({ providedIn: 'root' })
export class ReportApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  generate(request: GenerateReportRequest): Observable<ReportSummary> {
    return this.http
      .post<ApiResponse<ReportSummary>>(`${this.configuration.apiBaseUrl}/reports`, request)
      .pipe(map((response) => response.data));
  }

  listHistory(): Observable<ReportSummary[]> {
    return this.http
      .get<ApiResponse<ReportSummary[]>>(`${this.configuration.apiBaseUrl}/reports`)
      .pipe(map((response) => response.data));
  }

  getReport(id: string): Observable<ReportSummary> {
    return this.http
      .get<ApiResponse<ReportSummary>>(`${this.configuration.apiBaseUrl}/reports/${id}`)
      .pipe(map((response) => response.data));
  }

  download(id: string, fallbackFileName = 'report'): void {
    const url = `${this.configuration.apiBaseUrl}/reports/${id}/download`;
    this.http.get(url, { responseType: 'blob', observe: 'response' }).subscribe((response) => {
      const blob = response.body;
      if (!blob) {
        return;
      }
      const disposition = response.headers.get('Content-Disposition');
      let fileName = fallbackFileName;
      if (disposition) {
        const match = /filename="?([^"]+)"?/i.exec(disposition);
        if (match?.[1]) {
          fileName = match[1];
        }
      }
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = objectUrl;
      anchor.download = fileName;
      anchor.click();
      URL.revokeObjectURL(objectUrl);
    });
  }
}
