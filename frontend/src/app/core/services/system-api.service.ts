import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { SystemInfo } from '../models/system-info.model';
import { ConfigurationService } from './configuration.service';

/**
 * HTTP client for foundation system endpoints.
 * No business calculations — presentation data only.
 */
@Injectable({ providedIn: 'root' })
export class SystemApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  getSystemInfo(): Observable<SystemInfo> {
    const url = `${this.configuration.apiBaseUrl}/system/info`;
    return this.http
      .get<ApiResponse<SystemInfo>>(url)
      .pipe(map((response) => response.data));
  }
}
