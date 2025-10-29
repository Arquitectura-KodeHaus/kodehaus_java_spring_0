import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';
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
  private userSubscription!: Subscription;

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    // Suscribirse a cambios en el usuario
    this.userSubscription = this.authService.user$.subscribe((user) => {
      this.user = user;
    });
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

