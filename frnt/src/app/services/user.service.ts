import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  plazaId: number;
  roleIds: number[];
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  fullName: string;
  plazaId: number;
  plazaName: string;
  roles: Role[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = `${environment.apiUrl}/api/users`;
  private readonly ROLES_URL = `${environment.apiUrl}/api/roles`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los usuarios
   */
  getUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.API_URL);
  }

  /**
   * Obtiene un usuario por ID
   */
  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Crea un nuevo usuario
   */
  createUser(userRequest: UserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.API_URL, userRequest);
  }

  /**
   * Actualiza un usuario existente
   */
  updateUser(id: number, userRequest: Partial<UserRequest>): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.API_URL}/${id}`, userRequest);
  }

  /**
   * Elimina un usuario (soft delete)
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Obtiene todos los roles disponibles
   */
  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(this.ROLES_URL);
  }

  /**
   * Obtiene un rol por ID
   */
  getRoleById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.ROLES_URL}/${id}`);
  }
}
