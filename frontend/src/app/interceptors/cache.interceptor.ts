// src/app/interceptors/cache.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const cacheInterceptor: HttpInterceptorFn = (request, next) => {
  // Clone the request and add cache control headers
  const modifiedRequest = request.clone({
    setHeaders: {
      'Cache-Control': 'no-cache, no-store, must-revalidate, post-check=0, pre-check=0',
      'Pragma': 'no-cache',
      'Expires': '0'
    }
  });

  return next(modifiedRequest);
};