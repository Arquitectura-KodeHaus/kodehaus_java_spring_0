import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

export interface PlazaDto {
  id?: number;
  name: string;
  description?: string;
  address: string;
  phoneNumber: string;
  email?: string;
  openingHours: string; // HH:mm
  closingHours: string; // HH:mm
  isActive?: boolean;
}

@Injectable({ providedIn: 'root' })
export class PlazaService {
  private readonly API_URL = environment.apiUrl; // ✅ Usar environment

  constructor(private http: HttpClient) {
    console.log('🏢 PlazaService initialized with API_URL:', this.API_URL);
  }

  getPlazas(): Observable<PlazaDto[]> {
    return this.http.get<PlazaDto[]>(`${this.API_URL}/plazas`);
  }

  getPlaza(id: number): Observable<PlazaDto> {
    return this.http.get<PlazaDto>(`${this.API_URL}/plazas/${id}`);
  }

  createPlaza(plaza: PlazaDto): Observable<PlazaDto> {
    return this.http.post<PlazaDto>(`${this.API_URL}/plazas`, plaza);
  }

  updatePlaza(id: number, plaza: PlazaDto): Observable<PlazaDto> {
    return this.http.put<PlazaDto>(`${this.API_URL}/plazas/${id}`, plaza);
  }

  getBoletin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/boletin`);
  }

  getLocales(): Observable<any[]> {
    // Use stores endpoint - the backend should handle /api/locales via StoreController
    // For now, using /api/stores directly
    return this.http.get<any[]>(`${this.API_URL}/stores`);
  }

  getPagos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/pagos`);
  }

  getParqueadero(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/parqueadero`);
  }
}
