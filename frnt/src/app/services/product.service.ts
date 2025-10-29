import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

export interface ProductRequest {
  name: string;
  description?: string;
  category: string;
  unit: string;
  price: number;
  isAvailable: boolean;
}

export interface ProductResponse {
  id: number;
  name: string;
  description?: string;
  category: string;
  unit: string;
  price: number;
  isActive: boolean;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
  plazaId: number;
  plazaName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly API_URL = `${environment.apiUrl}/products`; // ✅ Usar environment

  constructor(private http: HttpClient) {
    console.log('📦 ProductService initialized with API_URL:', this.API_URL);
  }

  /**
   * Obtiene todos los productos
   */
  getProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(this.API_URL);
  }

  /**
   * Obtiene solo los productos disponibles
   */
  getAvailableProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(`${this.API_URL}/available`);
  }

  /**
   * Obtiene las categorías de productos
   */
  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/categories`);
  }

  /**
   * Obtiene un producto por ID
   */
  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Crea un nuevo producto
   */
  createProduct(productRequest: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.API_URL, productRequest);
  }

  /**
   * Actualiza un producto existente
   */
  updateProduct(id: number, productRequest: Partial<ProductRequest>): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.API_URL}/${id}`, productRequest);
  }

  /**
   * Actualiza solo el precio de un producto
   */
  updateProductPrice(id: number, price: number): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.API_URL}/${id}/price`, { price });
  }

  /**
   * Elimina un producto (soft delete)
   */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
