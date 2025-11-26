import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor HTTP que añade automáticamente el token JWT a todas las peticiones
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // Skip adding token to login/auth endpoints (they don't need it)
    const isAuthEndpoint = req.url.includes('/auth/login') || req.url.includes('/auth/external-register');

    // Si hay token y no es un endpoint de autenticación, clonar la request y añadir el header Authorization
    const request = token && !isAuthEndpoint
      ? req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        })
      : req;

    // Log for debugging (can be removed in production)
    if (token && !isAuthEndpoint) {
      console.log('🔑 AuthInterceptor: Adding token to request:', req.url);
    } else if (!token && !isAuthEndpoint) {
      console.warn('⚠️ AuthInterceptor: No token available for request:', req.url);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 || error.status === 403) {
          console.error('❌ AuthInterceptor: Unauthorized/Forbidden - logging out');
          // Token inválido/expirado o sin permisos: limpiar sesión y redirigir a login
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
