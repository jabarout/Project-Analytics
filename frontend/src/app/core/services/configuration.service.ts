import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

/**
 * Application configuration for non-sensitive frontend settings.
 */
@Injectable({ providedIn: 'root' })
export class ConfigurationService {
  readonly apiBaseUrl = environment.apiBaseUrl;
  readonly applicationName = environment.applicationName;
  readonly production = environment.production;
}
