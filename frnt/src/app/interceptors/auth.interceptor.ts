import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor HTTP que añade automáticamente el token JWT a todas las peticiones
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log('Interceptor - Request URL:', req.url);
  console.log('Interceptor - Token available:', !!token);

  // Si hay token, clonar la request y añadir el header Authorization
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('Interceptor - Request headers:', cloned.headers.keys());
    return next(cloned);
  }

  // Si no hay token, enviar la request sin modificaciones
  console.log('Interceptor - No token available, sending request without authorization header');
  return next(req);
};