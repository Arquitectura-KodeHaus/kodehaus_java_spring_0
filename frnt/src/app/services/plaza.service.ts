import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PlazaDto {
  id?: number;
  nombre: string;
  direccion: string;
  contacto: string;
  horario: string;
}

@Injectable({ providedIn: 'root' })
export class PlazaService {
  private apiUrl = 'http://localhost:8080/api';

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

  deletePlaza(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/plazas/${id}`);
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
