import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';
import { ModuleService, ModuleDto } from '../../services/module.service';
import { Subscription } from 'rxjs';

/**
 * Componente de header que muestra información del usuario y botón de logout
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  user: User | null = null;
  availableModules: ModuleDto[] = [];
  private userSubscription!: Subscription;

  constructor(
    public authService: AuthService,
    private moduleService: ModuleService
  ) {}

  ngOnInit(): void {
    // Suscribirse a cambios en el usuario
    this.userSubscription = this.authService.user$.subscribe((user) => {
      this.user = user;
      if (user) {
        // Load modules from service (already loaded by AuthService after login)
        this.loadModulesFromService();
      }
    });
    
    // Load modules if user is already logged in
    if (this.authService.isLoggedIn()) {
      this.loadModulesFromService();
    }
  }
  
  private loadModulesFromService(): void {
    // Don't call backend directly - modules are already loaded by AuthService
    // Just get them from the service
    this.availableModules = this.moduleService.getAvailableModules();
  }
  
  /**
   * Check if a module is available
   */
  hasModule(moduleName: string): boolean {
    return this.moduleService.hasModule(moduleName);
  }
  
  /**
   * Get module route
   */
  getModuleRoute(moduleName: string): string {
    return this.moduleService.getModuleRoute(moduleName) || `/${moduleName.toLowerCase()}`;
  }

  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  /**
   * Maneja el logout del usuario
   */
  logout(): void {
    this.authService.logout();
  }

  /**
   * Convierte el nombre técnico del rol a un nombre más amigable
   */
  getRoleDisplayName(role: string): string {
    const roleMap: { [key: string]: string } = {
      'EMPLOYEE_SECURITY': 'Seguridad',
      'EMPLOYEE_PARKING': 'Parqueaderos',
      'EMPLOYEE_GENERAL': 'Empleado',
      'MANAGER': 'Manager',
      'ADMIN': 'Administrador'
    };
    return roleMap[role] || role;
  }

  /**
   * Obtiene los roles del usuario en formato amigable
   */
  getUserRolesDisplay(): string {
    if (!this.user || !this.user.roles) {
      return '';
    }
    return this.user.roles.map(role => this.getRoleDisplayName(role)).join(', ');
  }
}

