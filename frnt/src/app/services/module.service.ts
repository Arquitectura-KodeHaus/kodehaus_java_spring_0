import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ModuleDto {
  id: string;
  name: string;
  description?: string;
  enabled: boolean;
  route?: string;
  icon?: string;
}

@Injectable({ providedIn: 'root' })
export class ModuleService {
  private readonly API_URL = environment.apiUrl;
  private modules: ModuleDto[] = [];

  constructor(private http: HttpClient) {}

  getModules(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/modules`);
  }

  /**
   * Map backend module response to ModuleDto
   */
  private mapToModuleDto(module: any): ModuleDto {
    // Backend returns: {id: number, nombre: string, estado: string, descripcion: string}
    const enabled = module.estado ? module.estado.toLowerCase() === 'activo' : true;
    const moduleName = module.nombre || module.name || '';
    
    return {
      id: String(module.id || ''),
      name: moduleName,
      description: module.descripcion || module.description || '',
      enabled: enabled,
      route: module.route || `/${moduleName.toLowerCase()}`,
      icon: module.icon || ''
    };
  }

  setModules(modules: any[]): void {
    this.modules = modules.map(m => this.mapToModuleDto(m));
  }

  getAvailableModules(): ModuleDto[] {
    return this.modules.filter(m => m.enabled);
  }

  hasModule(moduleName: string): boolean {
    if (this.modules.length === 0) {
      // If no modules loaded, show all by default (backward compatibility)
      return true;
    }
    return this.modules.some(m => 
      (m.name.toLowerCase() === moduleName.toLowerCase() || 
       m.name === moduleName.toUpperCase()) && m.enabled
    );
  }

  getModuleRoute(moduleName: string): string | undefined {
    const module = this.modules.find(m => m.name === moduleName && m.enabled);
    return module?.route;
  }
}

