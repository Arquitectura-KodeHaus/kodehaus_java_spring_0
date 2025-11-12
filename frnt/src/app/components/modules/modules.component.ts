import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ModuleService, ModuleDto } from '../../services/module.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-modules',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="modules-container">
      <div class="page-header">
        <h2>Módulos Disponibles</h2>
        <p class="subtitle">Módulos habilitados para tu plaza</p>
      </div>

      <div *ngIf="isLoading" class="loading">
        <p>Cargando módulos...</p>
      </div>

      <div *ngIf="!isLoading && errorMessage" class="error-message">
        <p>{{ errorMessage }}</p>
      </div>

      <div *ngIf="!isLoading && modules.length === 0 && !errorMessage" class="no-modules">
        <div class="empty-state">
          <h3>No hay módulos disponibles</h3>
          <p>Los módulos aparecerán aquí una vez que sean habilitados para tu plaza.</p>
          <div class="empty-icon">📦</div>
        </div>
      </div>

      <div *ngIf="!isLoading && modules.length > 0" class="modules-grid">
        <div *ngFor="let module of modules" class="module-card" [class.enabled]="module.enabled" [class.disabled]="!module.enabled">
          <div class="module-icon" *ngIf="module.icon">
            {{ module.icon }}
          </div>
          <div class="module-content">
            <h3 class="module-name">{{ module.name }}</h3>
            <p class="module-description" *ngIf="module.description">{{ module.description }}</p>
            <div class="module-status">
              <span class="status-badge" [class.active]="module.enabled" [class.inactive]="!module.enabled">
                {{ module.enabled ? 'Habilitado' : 'Deshabilitado' }}
              </span>
            </div>
            <div class="module-route" *ngIf="module.route && module.enabled">
              <a [routerLink]="module.route" class="module-link">Acceder →</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modules-container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 1rem;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h2 {
      margin: 0 0 0.5rem 0;
      color: #333;
    }

    .subtitle {
      color: #6c757d;
      margin: 0;
    }

    .loading {
      text-align: center;
      padding: 3rem;
      color: #6c757d;
    }

    .error-message {
      padding: 1rem;
      background: #f8d7da;
      color: #721c24;
      border-radius: 4px;
      margin-bottom: 2rem;
    }

    .no-modules {
      text-align: center;
      padding: 3rem;
    }

    .empty-state {
      max-width: 500px;
      margin: 0 auto;
    }

    .empty-state h3 {
      color: #6c757d;
      margin-bottom: 1rem;
    }

    .empty-state p {
      color: #6c757d;
      margin-bottom: 2rem;
    }

    .empty-icon {
      font-size: 4rem;
      opacity: 0.5;
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-top: 2rem;
    }

    .module-card {
      background: white;
      border: 2px solid #e9ecef;
      border-radius: 8px;
      padding: 1.5rem;
      transition: all 0.3s ease;
      display: flex;
      flex-direction: column;
    }

    .module-card.enabled {
      border-color: #28a745;
      box-shadow: 0 2px 8px rgba(40, 167, 69, 0.1);
    }

    .module-card.disabled {
      border-color: #dc3545;
      opacity: 0.7;
    }

    .module-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .module-icon {
      font-size: 3rem;
      text-align: center;
      margin-bottom: 1rem;
    }

    .module-content {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .module-name {
      margin: 0 0 0.5rem 0;
      color: #333;
      font-size: 1.25rem;
    }

    .module-description {
      color: #6c757d;
      margin: 0 0 1rem 0;
      flex: 1;
    }

    .module-status {
      margin-bottom: 1rem;
    }

    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .status-badge.active {
      background: #d4edda;
      color: #155724;
    }

    .status-badge.inactive {
      background: #f8d7da;
      color: #721c24;
    }

    .module-route {
      margin-top: auto;
    }

    .module-link {
      display: inline-block;
      color: #007bff;
      text-decoration: none;
      font-weight: 500;
      transition: color 0.2s;
    }

    .module-link:hover {
      color: #0056b3;
      text-decoration: underline;
    }

    @media (max-width: 768px) {
      .modules-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ModulesComponent implements OnInit {
  modules: ModuleDto[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private moduleService: ModuleService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.errorMessage = 'Debes iniciar sesión para ver los módulos';
      this.isLoading = false;
      return;
    }
    
    this.loadModules();
  }

  loadModules(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // First, try to get modules from service (already loaded by AuthService)
    const cachedModules = this.moduleService.getAvailableModules();
    
    if (cachedModules.length > 0) {
      // Use cached modules
      this.modules = cachedModules;
      this.isLoading = false;
      return;
    }

    // If no cached modules, fetch from backend
    this.moduleService.getModules().subscribe({
      next: (modules: any[]) => {
        this.isLoading = false;
        if (modules && Array.isArray(modules)) {
          this.moduleService.setModules(modules);
          this.modules = this.moduleService.getAvailableModules();
          
          if (this.modules.length === 0) {
            // Si no hay módulos, podría ser que el servicio externo no esté disponible
            // o que la plaza no tenga externalId
            this.errorMessage = 'No se pudieron cargar los módulos. Verifica que tu plaza tenga un ID externo configurado.';
          }
        } else {
          this.modules = [];
          this.errorMessage = 'Formato de respuesta inválido';
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error cargando módulos:', error);
        this.errorMessage = 'Error al cargar los módulos. Esto puede deberse a que el servicio externo no está disponible.';
        this.modules = [];
      }
    });
  }
}

