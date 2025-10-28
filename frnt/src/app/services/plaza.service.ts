import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PlazaDto {
  id?: number;
  name: string;
  description?: string;
  address: string;
  phoneNumber: string;
  email?: string;
  openingHours: string; // HH:mm
  closingHours: string; // HH:mm
}

@Injectable({ providedIn: 'root' })
export class PlazaService {
  // Backend corre en 8081 según application.properties
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getPlazas(): Observable<PlazaDto[]> {
    return this.http.get<PlazaDto[]>(`${this.apiUrl}/plazas`);
  }

  getPlaza(id: number): Observable<PlazaDto> {
    return this.http.get<PlazaDto>(`${this.apiUrl}/plazas/${id}`);
  }

  createPlaza(plaza: PlazaDto): Observable<PlazaDto> {
    return this.http.post<PlazaDto>(`${this.apiUrl}/plazas`, plaza);
  }

  updatePlaza(id: number, plaza: PlazaDto): Observable<PlazaDto> {
    return this.http.put<PlazaDto>(`${this.apiUrl}/plazas/${id}`, plaza);
  }

  getBoletin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/boletin`);
  }

  getLocales(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/locales`);
  }

  getPagos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/pagos`);
  }

  getParqueadero(): Observable<any[]> {
    return this.http.get<any[]>((`${this.apiUrl}/parqueadero`));
  }
}
