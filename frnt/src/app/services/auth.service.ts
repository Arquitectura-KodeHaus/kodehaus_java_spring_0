import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, map } from 'rxjs/operators';

export interface User {
  id: number;
  username: string;
  roles: string[];
  permissions: string[];
  plazaId: number;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'auth_token';
  private userKey = 'auth_user';
  private apiUrl = '/api/auth/login';
  private _user: User | null = null;

  constructor(private http: HttpClient) {
    this.loadUser();
  }

  login(username: string, password: string): Observable<boolean> {
    return this.http.post<any>(this.apiUrl, { username, password }).pipe(
      tap(res => {
        if (res.token && res.user) {
          localStorage.setItem(this.tokenKey, res.token);
          localStorage.setItem(this.userKey, JSON.stringify(res.user));
          this._user = res.user;
        }
      }),
      map(res => !!res.token)
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this._user = null;
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get user(): User | null {
    return this._user;
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }

  hasPermission(permission: string): boolean {
    return this._user?.permissions.includes(permission) ?? false;
  }

  getPlazaId(): number | null {
    return this._user?.plazaId ?? null;
  }

  getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.token}`
    });
  }

  private loadUser() {
    const userStr = localStorage.getItem(this.userKey);
    if (userStr) {
      this._user = JSON.parse(userStr);
    }
  }
}
