
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BulletinDto {
  id?: number;
  title: string;
  content?: string;
  publicationDate?: string;
  plazaId: number;
  fileName?: string;
  filePath?: string;
  fileType?: string;
  fileSize?: number;
}

export interface BulletinResponseDto {
  id: number;
  title: string;
  content?: string;
  publicationDate: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  plazaId: number;
  plazaName: string;
  createdById: number;
  createdByUsername: string;
  createdByFullName: string;
  fileName?: string;
  filePath?: string;
  fileType?: string;
  fileSize?: number;
}

@Injectable({ providedIn: 'root' })
export class BulletinService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getBulletins(): Observable<BulletinResponseDto[]> {
    return this.http.get<BulletinResponseDto[]>(`${this.apiUrl}/bulletins`);
  }

  getBulletinById(id: number): Observable<BulletinResponseDto> {
    return this.http.get<BulletinResponseDto>(`${this.apiUrl}/bulletins/${id}`);
  }

  getTodaysBulletins(): Observable<BulletinResponseDto[]> {
    return this.http.get<BulletinResponseDto[]>(`${this.apiUrl}/bulletins/today`);
  }

  getBulletinsByDate(date: string): Observable<BulletinResponseDto[]> {
    return this.http.get<BulletinResponseDto[]>(`${this.apiUrl}/bulletins/date/${date}`);
  }

  createBulletin(bulletin: BulletinDto): Observable<BulletinResponseDto> {
    return this.http.post<BulletinResponseDto>(`${this.apiUrl}/bulletins`, bulletin);
  }

  createBulletinWithFile(formData: FormData): Observable<BulletinResponseDto> {
    console.log('Enviando request a:', `${this.apiUrl}/bulletins/with-file`);
    console.log('FormData keys:', Array.from(formData.keys()));
    
    // Log contents of FormData
    for (const key of formData.keys()) {
      const value = formData.get(key);
      console.log(`FormData[${key}]:`, value instanceof File ? `File: ${(value as File).name}` : value);
    }
    
    return this.http.post<BulletinResponseDto>(`${this.apiUrl}/bulletins/with-file`, formData);
  }

  updateBulletin(id: number, bulletin: BulletinDto): Observable<BulletinResponseDto> {
    return this.http.put<BulletinResponseDto>(`${this.apiUrl}/bulletins/${id}`, bulletin);
  }

  deleteBulletin(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bulletins/${id}`);
  }

  downloadBulletinFile(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/bulletins/${id}/file`, { responseType: 'blob' });
  }
}
