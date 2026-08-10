/**
 * Mirrors the backend standard success envelope (API Specification).
 */
export interface ApiResponse<T> {
  readonly success: boolean;
  readonly data: T;
  readonly timestamp: string;
}

/**
 * Mirrors the backend standard error envelope (Error Catalog).
 */
export interface ApiErrorBody {
  readonly code: string;
  readonly message: string;
  readonly details?: readonly string[];
}

export interface ErrorResponse {
  readonly success: false;
  readonly error: ApiErrorBody;
  readonly timestamp: string;
  readonly path?: string;
}
