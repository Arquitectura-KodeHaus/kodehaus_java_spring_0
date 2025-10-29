# 🎯 Ejemplos de Uso: Roles y Permisos en Angular

Este documento muestra ejemplos prácticos de cómo usar el sistema de roles y permisos en tu aplicación Angular.

---

## 📋 Índice

1. [Ejemplos en Templates HTML](#ejemplos-en-templates-html)
2. [Ejemplos en Componentes TypeScript](#ejemplos-en-componentes-typescript)
3. [Manejo de Estados](#manejo-de-estados)
4. [Condicionales Avanzadas](#condicionales-avanzadas)
5. [Patrones Recomendados](#patrones-recomendados)

---

## 1️⃣ Ejemplos en Templates HTML

### Mostrar contenido solo para ADMIN

```html
<!-- Botón solo visible para ADMIN -->
<button 
  *ngIf="authService.hasRole('ADMIN')" 
  class="admin-btn">
  Panel de Administración
</button>

<!-- O usando roles múltiples -->
<div *ngIf="authService.hasRole('ADMIN') || authService.hasRole('MANAGER')">
  Contenido para administradores y managers
</div>
```

### Verificar permisos en el template

```html
<!-- Mostrar formulario solo si tiene permiso de escritura -->
<div *ngIf="authService.can('plaza:write')">
  <form [formGroup]="plazaForm">
    <!-- Campos del formulario -->
  </form>
</div>

<!-- Botón deshabilitado sin permisos -->
<button 
  [disabled]="!authService.can('user:write')"
  (click)="saveUser()">
  Guardar Usuario
</button>
```

### Información del usuario

```html
<!-- Mostrar datos del usuario -->
<div *ngIf="authService.getUser()">
  <h2>Bienvenido, {{ authService.getUser()?.fullName }}</h2>
  
  <!-- Roles como badges -->
  <div class="roles">
    <span 
      *ngFor="let role of authService.getUser()?.roles" 
      class="badge">
      {{ role }}
    </span>
  </div>
  
  <!-- Información de plaza -->
  <p *ngIf="authService.getUser()?.plazaName">
    Plaza: {{ authService.getUser()?.plazaName }}
  </p>
</div>
```

### Navegación condicional

```html
<nav>
  <a routerLink="/plaza">Plaza</a>
  <a routerLink="/boletin" *ngIf="authService.can('bulletin:read')">
    Boletín
  </a>
  <a routerLink="/locales" *ngIf="authService.can('plaza:read')">
    Locales
  </a>
  <a routerLink="/admin" *ngIf="authService.isAdmin">
    Administración
  </a>
</nav>
```

---

## 2️⃣ Ejemplos en Componentes TypeScript

### Verificar rol al inicializar

```typescript
import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-mi-componente',
  templateUrl: './mi-componente.component.html'
})
export class MiComponente implements OnInit {
  isAdmin = false;
  canEdit = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Verificar si es administrador
    this.isAdmin = this.authService.isAdmin;
    
    // Verificar permiso específico
    this.canEdit = this.authService.can('plaza:write');
    
    // Verificar rol específico
    if (this.authService.hasRole('MANAGER')) {
      console.log('Usuario es Manager');
    }
  }
}
```

### Obtener información del usuario

```typescript
import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../services/auth.service';

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  user: User | null = null;
  userRoles: string[] = [];
  userPermissions: string[] = [];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    
    if (this.user) {
      this.userRoles = this.user.roles;
      this.userPermissions = this.user.permissions;
      
      console.log('Nombre:', this.user.fullName);
      console.log('Email:', this.user.email);
      console.log('Plaza:', this.user.plazaName);
      console.log('Roles:', this.user.roles);
      console.log('Permisos:', this.user.permissions);
    }
  }
}
```

### Proteger métodos con permisos

```typescript
import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-plaza',
  templateUrl: './plaza.component.html'
})
export class PlazaComponent {
  constructor(private authService: AuthService) {}

  deletePlaza(id: number): void {
    // Verificar permiso antes de ejecutar
    if (!this.authService.can('plaza:delete')) {
      alert('No tienes permiso para eliminar plazas');
      return;
    }

    // Lógica de eliminación
    if (confirm('¿Seguro que deseas eliminar esta plaza?')) {
      // eliminar...
    }
  }

  editPlaza(id: number): void {
    if (this.authService.can('plaza:write')) {
      this.startEdit(id);
    } else {
      this.showReadOnlyMessage();
    }
  }

  private showReadOnlyMessage(): void {
    alert('Solo puedes ver esta información, no editarla');
  }

  private startEdit(id: number): void {
    // iniciar edición...
  }
}
```

---

## 3️⃣ Manejo de Estados

### Suscribirse a cambios en el usuario

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService, User } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private userSubscription?: Subscription;
  currentUser: User | null = null;
  isAuthenticated = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Suscribirse a cambios en el usuario
    this.userSubscription = this.authService.user$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
      
      if (user) {
        console.log('Usuario autenticado:', user.fullName);
        this.loadUserData(user);
      } else {
        console.log('Usuario deslogueado');
        this.clearUserData();
      }
    });
  }

  ngOnDestroy(): void {
    // Importante: siempre desuscribirse para evitar memory leaks
    this.userSubscription?.unsubscribe();
  }

  private loadUserData(user: User): void {
    // Cargar datos específicos del usuario
    console.log('Cargando datos para:', user.username);
  }

  private clearUserData(): void {
    // Limpiar datos del usuario
    console.log('Limpiando datos del usuario');
  }
}
```

---

## 4️⃣ Condicionales Avanzadas

### Template con múltiples condiciones

```html
<!-- Ejemplo: Mostrar diferentes acciones según el rol -->
<div class="actions">
  
  <!-- Para ADMIN: todas las acciones -->
  <button *ngIf="authService.isAdmin" class="admin-action">
    Configuración Avanzada
  </button>
  
  <!-- Para MANAGER: acciones de gestión -->
  <button 
    *ngIf="authService.hasRole('MANAGER') && !authService.isAdmin"
    class="manager-action">
    Gestionar Equipo
  </button>
  
  <!-- Para cualquier empleado: acciones básicas -->
  <button 
    *ngIf="authService.can('bulletin:read')"
    class="employee-action">
    Ver Boletines
  </button>
  
  <!-- Acción solo si tiene múltiples permisos -->
  <button 
    *ngIf="authService.can('plaza:read') && authService.can('user:read')"
    class="reports-action">
    Generar Reportes
  </button>
  
</div>
```

### Ejemplo: Formulario con estados

```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-configuracion',
  templateUrl: './configuracion.component.html'
})
export class ConfiguracionComponent implements OnInit {
  configForm!: FormGroup;
  isReadOnly = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.configForm = this.fb.group({
      nombre: [''],
      direccion: [''],
      // más campos...
    });

    // Determinar si puede editar
    this.isReadOnly = !this.authService.can('plaza:write');
    
    // Deshabilitar formulario si no tiene permisos
    if (this.isReadOnly) {
      this.configForm.disable({ emitEvent: false });
    }

    this.loadConfiguration();
  }

  loadConfiguration(): void {
    // Cargar configuración...
  }

  saveConfiguration(): void {
    if (!this.canEdit()) {
      alert('No tienes permisos para editar esta configuración');
      return;
    }

    if (this.configForm.valid) {
      // Guardar...
    }
  }

  private canEdit(): boolean {
    return this.authService.can('plaza:write');
  }
}
```

---

## 5️⃣ Patrones Recomendados

### Servicio de permisos personalizado (Opcional)

Si necesitas lógica más compleja, puedes crear un servicio dedicado:

```typescript
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  constructor(private authService: AuthService) {}

  canAccessAdminPanel(): boolean {
    return this.authService.hasRole('ADMIN') || 
           this.authService.hasRole('MANAGER');
  }

  canManageUsers(): boolean {
    return this.authService.can('user:write');
  }

  canViewReports(): boolean {
    return this.authService.can('reports:read');
  }

  canDeleteResources(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  getVisibleMenuItems(): string[] {
    const items: string[] = ['home'];
    
    if (this.authService.can('plaza:read')) {
      items.push('plaza');
    }
    
    if (this.authService.can('bulletin:read')) {
      items.push('boletin');
    }
    
    if (this.authService.can('user:read')) {
      items.push('users');
    }
    
    if (this.authService.isAdmin) {
      items.push('admin');
    }
    
    return items;
  }
}
```

### Uso del servicio personalizado:

```typescript
import { PermissionService } from './permission.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html'
})
export class NavigationComponent {
  constructor(private permissionService: PermissionService) {}

  get visibleItems(): string[] {
    return this.permissionService.getVisibleMenuItems();
  }
}
```

```html
<!-- En el template -->
<nav>
  <a *ngFor="let item of visibleItems" 
     [routerLink]="item">
    {{ item | titlecase }}
  </a>
</nav>
```

---

## 🎨 Ejemplo Completo: Componente con Gestión de Permisos

```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, User } from '../../services/auth.service';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-plaza-gestion',
  templateUrl: './plaza-gestion.component.html'
})
export class PlazaGestionComponent implements OnInit {
  plazas: any[] = [];
  form!: FormGroup;
  selectedPlaza?: any;
  
  // Flags de permisos
  canView = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;
  
  currentUser: User | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private plazaService: PlazaService
  ) {}

  ngOnInit(): void {
    // Obtener usuario actual
    this.currentUser = this.authService.getUser();
    
    // Verificar permisos
    this.canView = this.authService.can('plaza:read');
    this.canCreate = this.authService.can('plaza:create');
    this.canEdit = this.authService.can('plaza:write');
    this.canDelete = this.authService.can('plaza:delete');

    // Inicializar formulario
    this.form = this.fb.group({
      id: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      direccion: ['', Validators.required],
      contacto: ['', Validators.required]
    });

    // Deshabilitar formulario si no puede editar
    if (!this.canEdit) {
      this.form.disable({ emitEvent: false });
    }

    // Solo cargar datos si puede ver
    if (this.canView) {
      this.loadPlazas();
    }
  }

  loadPlazas(): void {
    this.plazaService.getPlazas().subscribe({
      next: (data) => this.plazas = data,
      error: (err) => console.error('Error cargando plazas:', err)
    });
  }

  save(): void {
    if (!this.canEdit) {
      alert('No tienes permisos para editar plazas');
      return;
    }

    if (this.form.valid) {
      const plazaData = this.form.value;
      
      if (plazaData.id) {
        // Actualizar
        this.plazaService.updatePlaza(plazaData.id, plazaData).subscribe({
          next: () => this.loadPlazas(),
          error: (err) => console.error('Error actualizando:', err)
        });
      } else {
        // Crear
        if (!this.canCreate) {
          alert('No tienes permisos para crear plazas');
          return;
        }
        
        this.plazaService.createPlaza(plazaData).subscribe({
          next: () => this.loadPlazas(),
          error: (err) => console.error('Error creando:', err)
        });
      }
    }
  }

  delete(plaza: any): void {
    if (!this.canDelete) {
      alert('No tienes permisos para eliminar plazas');
      return;
    }

    if (confirm(`¿Estás seguro de eliminar ${plaza.nombre}?`)) {
      this.plazaService.deletePlaza(plaza.id).subscribe({
        next: () => this.loadPlazas(),
        error: (err) => console.error('Error eliminando:', err)
      });
    }
  }
}
```

```html
<!-- Template HTML -->
<div class="plaza-gestion">
  
  <!-- Mensaje de permisos -->
  <div class="permission-badge" *ngIf="!canEdit">
    <p>⚠️ Solo lectura: No tienes permisos para editar</p>
  </div>

  <!-- Lista de plazas -->
  <div class="plazas-list">
    <div *ngFor="let plaza of plazas" class="plaza-card">
      <h3>{{ plaza.nombre }}</h3>
      <p>{{ plaza.direccion }}</p>
      
      <div class="actions" *ngIf="canEdit || canDelete">
        <button 
          *ngIf="canEdit" 
          (click)="selectPlaza(plaza)"
          class="btn-edit">
          Editar
        </button>
        <button 
          *ngIf="canDelete" 
          (click)="delete(plaza)"
          class="btn-delete">
          Eliminar
        </button>
      </div>
    </div>
  </div>

  <!-- Formulario -->
  <form [formGroup]="form" (ngSubmit)="save()">
    <!-- Campos del formulario -->
    <input formControlName="nombre" placeholder="Nombre" />
    <input formControlName="direccion" placeholder="Dirección" />
    <input formControlName="contacto" placeholder="Contacto" />
    
    <!-- Botones -->
    <div class="form-actions">
      <button 
        type="submit" 
        [disabled]="!canEdit || form.invalid"
        class="btn-save">
        Guardar
      </button>
    </div>
  </form>

</div>
```

---

## 📚 Resumen

✅ **En Templates**: Usa `*ngIf` con `authService.hasRole()`, `authService.can()`, etc.

✅ **En Componentes**: Verifica permisos en `ngOnInit()` y guarda en variables de clase.

✅ **Gestión de Estado**: Suscríbete a `authService.user$` para reaccionar a cambios.

✅ **Clean Code**: Crea métodos helper para lógica compleja de permisos.

✅ **Seguridad**: Siempre valida permisos en el servidor también.

---

¡Feliz desarrollo! 🚀

