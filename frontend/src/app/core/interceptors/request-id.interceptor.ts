import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Adds a client-generated request identifier for correlation with backend logs.
 * Authentication interceptors are introduced in Milestone 2.
 */
export const requestIdInterceptor: HttpInterceptorFn = (request, next) => {
  const requestId =
    typeof crypto !== 'undefined' && 'randomUUID' in crypto
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

  return next(
    request.clone({
      setHeaders: {
        'X-Request-Id': requestId,
      },
    })
  );
};
