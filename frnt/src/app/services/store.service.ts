import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface StoreDto {
  id?: number;
  externalId?: string;
  name: string;
  description?: string;
  ownerName?: string;
  phoneNumber?: string;
  email?: string;
  isActive?: boolean;
  plazaId?: number;
  plazaName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StoreOwnerDto {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

@Injectable({ providedIn: 'root' })
export class StoreService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getStores(): Observable<StoreDto[]> {
    return this.http.get<StoreDto[]>(`${this.API_URL}/stores`);
  }

  getStore(id: number): Observable<StoreDto> {
    return this.http.get<StoreDto>(`${this.API_URL}/stores/${id}`);
  }

  createStore(store: StoreDto): Observable<StoreDto> {
    return this.http.post<StoreDto>(`${this.API_URL}/stores`, store);
  }

  updateStore(id: number, store: StoreDto): Observable<StoreDto> {
    return this.http.put<StoreDto>(`${this.API_URL}/stores/${id}`, store);
  }

  deleteStore(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/stores/${id}`);
  }

  createStoreOwner(storeId: number, owner: StoreOwnerDto): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/stores/${storeId}/owner`, owner);
  }
}

