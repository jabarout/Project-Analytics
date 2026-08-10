/**
 * Mirrors backend SystemInfoResponse DTO.
 */
export interface SystemInfo {
  readonly application: string;
  readonly version: string;
  readonly environment: string;
  readonly apiVersion: string;
}
