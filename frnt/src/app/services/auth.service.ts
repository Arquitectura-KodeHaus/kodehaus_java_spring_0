import { Injectable, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../environments/environment.prod';
import { ModuleService } from './module.service';

/**
 * Interfaz para la respuesta del login del backend
 */
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  plazaId: number;
  plazaName: string;
  roles: string[];
  externalId: string;
}

/**
 * Interfaz para el payload del JWT decodificado
 */
interface JwtPayload {
  sub: string; // username
  roles?: string[];
  exp: number; // expiration timestamp
  iat: number; // issued at timestamp
  plazaId?: number;
  plazaName?: string;
  userId?: number;
}

/**
 * Interfaz para el usuario autenticado
 */
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  roles: string[];
  plazaId?: number;
  plazaName?: string;
  permissions: string[]; // e.g., ['plaza:read', 'plaza:write']
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly API_URL = environment.apiUrl; // ✅ Usar environment

  private userSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.userSubject.asObservable();
  private moduleService: ModuleService | null = null;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private injector: Injector
  ) {
    console.log('🔐 AuthService initialized with API_URL:', this.API_URL);
    this.loadUserFromStorage();
  }
  
  /**
   * Decodifica el JWT y devuelve su payload ya tipado
   */
  private decodeToken(token: string): JwtPayload | null {
    try {
      return jwtDecode<JwtPayload>(token);
    } catch (error) {
      console.error('Error decodificando token:', error);
      return null;
    }
  }

  /**
   * Normaliza la información del usuario combinando lo que llega del backend y lo que vive en el JWT
   */
  private buildUser(payload: Partial<LoginResponse> | null, decoded: JwtPayload | null): User {
    const roles = decoded?.roles ?? payload?.roles ?? [];
    const firstName = payload?.firstName ?? '';
    const lastName = payload?.lastName ?? '';
    const fullName = payload?.fullName ?? `${firstName} ${lastName}`.trim();

    return {
      id: payload?.id ?? decoded?.userId ?? 0,
      username: payload?.username ?? decoded?.sub ?? '',
      email: payload?.email ?? '',
      firstName,
      lastName,
      fullName,
      roles,
      plazaId: decoded?.plazaId ?? payload?.plazaId,
      plazaName: payload?.plazaName ?? decoded?.plazaName,
      permissions: this.mapRolesToPermissions(roles)
    };
  }

  /**
   * Lazy load ModuleService to avoid circular dependency
   */
  private getModuleService(): ModuleService {
    if (!this.moduleService) {
      this.moduleService = this.injector.get(ModuleService);
    }
    return this.moduleService;
  }

  /**
   * Inicia sesión con username y password
   * @param username Usuario
   * @param password Contraseña
   * @returns Observable con la respuesta del login
   */
  login(username: string, password: string): Observable<LoginResponse> {
    console.log('🔐 Login attempt:', { username, apiUrl: `${this.API_URL}/auth/login` });
    
    return this.http
      .post<LoginResponse>(`${this.API_URL}/auth/login`, { username, password })
      .pipe(
        tap((response) => {
          console.log('✅ Login successful:', response);
          
          // Guardar token y usuario en localStorage
          localStorage.setItem(this.TOKEN_KEY, response.accessToken);
<<<<<<< Updated upstream
          localStorage.setItem('external_id', response.externalId);
          localStorage.setItem(this.USER_KEY, JSON.stringify(response));
          

          // Mapear la respuesta del backend al formato User
          const user: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            firstName: response.firstName,
            lastName: response.lastName,
            fullName: response.fullName,
            roles: response.roles,
            plazaId: response.plazaId,
            plazaName: response.plazaName,
            permissions: this.mapRolesToPermissions(response.roles)
          };
=======
          const decoded = this.decodeToken(response.accessToken);
          const user: User = this.buildUser(response, decoded);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
>>>>>>> Stashed changes

          this.userSubject.next(user);
          
          // Load modules after successful login with a longer delay to ensure token is saved
          setTimeout(() => {
            this.loadModules();
          }, 500);
        }),
        catchError((error) => {
          console.error('❌ Error en login:', error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Load modules from the backend
   */
  private loadModules() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    try {
      const moduleService = this.getModuleService();
      moduleService.getModules().subscribe({
        next: (modules: any) => {
          if (modules && Array.isArray(modules)) {
            console.log('✅ Modules loaded from Auth:', modules);
            moduleService.setModules(modules);
          } else {
            console.log('⚠️ No modules received or invalid format, using empty array');
            moduleService.setModules([]);
          }
        },
        error: (error) => {
          console.error('❌ Error loading modules:', error);
          // Set empty modules array if error (will show all modules by default)
          moduleService.setModules([]);
        }
      });
    } catch (error) {
      console.error('Error getting ModuleService:', error);
      // Silently fail - modules are optional
    }
  }

  /**
   * Cierra sesión y limpia el storage
   */
  logout(): void {
    console.log('🚪 Logging out...');
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem('external_id');
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Obtiene el token JWT del localStorage
   * @returns Token JWT o null
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Verifica si el usuario está autenticado y el token es válido
   * @returns true si está autenticado y el token no ha expirado
   */
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    const decoded = this.decodeToken(token);
    if (!decoded) {
      return false;
    }

    const currentTime = Math.floor(Date.now() / 1000);
    return decoded.exp > currentTime;
  }

  /**
   * Obtiene información del usuario actual
   * @returns Usuario autenticado o null
   */
  getUser(): User | null {
    return this.userSubject.value;
  }

  /**
   * Carga el usuario desde localStorage al iniciar
   */
  private loadUserFromStorage(): void {
    const token = this.getToken();
    if (!token) {
      return;
    }

    const decoded = this.decodeToken(token);
    if (!decoded) {
      this.logout();
      return;
    }

    const currentTime = Math.floor(Date.now() / 1000);
    if (decoded.exp <= currentTime) {
      this.logout();
      return;
    }

    let payload: Partial<LoginResponse> | null = null;
    const userData = localStorage.getItem(this.USER_KEY);
    if (userData) {
      try {
        payload = JSON.parse(userData);
      } catch (error) {
        console.error('Error cargando usuario del storage:', error);
      }
    }

    const user = this.buildUser(payload, decoded);
    this.userSubject.next(user);
  }

  /**
   * Verifica si el usuario tiene un rol específico
   * @param role Rol a verificar
   * @returns true si el usuario tiene el rol
   */
  hasRole(role: string): boolean {
    const user = this.getUser();
    return user?.roles.includes(role) ?? false;
  }

  /**
   * Verifica si el usuario es ADMIN
   */
  get isAdmin(): boolean {
    return this.hasRole('ADMIN') || this.hasRole('MANAGER');
  }

  /**
   * Verifica si el usuario tiene un permiso
   * @param permission Permiso a verificar
   * @returns true si el usuario tiene el permiso
   */
  can(permission: string): boolean {
    const user = this.getUser();
    return user?.permissions?.includes(permission) ?? false;
  }

  /**
   * Obtiene el ID de la plaza del usuario
   */
  get plazaId(): number | undefined {
    return this.getUser()?.plazaId;
  }

  /**
   * Mapea roles del backend a permisos de la aplicación
   * @param roles Roles del usuario
   * @returns Array de permisos
   */
  private mapRolesToPermissions(roles: string[]): string[] {
    const permissions: string[] = [];

    if (roles.includes('MANAGER') || roles.includes('ADMIN')) {
      permissions.push('plaza:read', 'plaza:write', 'user:read', 'user:write');
    }

    if (roles.includes('EMPLOYEE_GENERAL')) {
      permissions.push('bulletin:read', 'bulletin:write');
    }

    if (roles.includes('EMPLOYEE_SECURITY')) {
      permissions.push('security:read', 'security:write');
    }

    if (roles.includes('EMPLOYEE_PARKING')) {
      permissions.push('parking:read', 'parking:write');
    }

    return permissions;
  }
}
