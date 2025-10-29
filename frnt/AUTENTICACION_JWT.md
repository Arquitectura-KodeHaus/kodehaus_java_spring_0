# 📋 Guía Completa: Sistema de Autenticación JWT en Angular

## 🎯 Descripción General

Este proyecto implementa un sistema completo de autenticación JWT para una aplicación Angular 17+ que se conecta a un backend Spring Boot. El sistema incluye:

- ✅ Login con validación de credenciales
- ✅ Almacenamiento seguro del token JWT
- ✅ Interceptor HTTP para añadir automáticamente el token a las peticiones
- ✅ Guards para proteger rutas
- ✅ Manejo de roles y permisos
- ✅ Header con información del usuario y logout
- ✅ Validación de expiración del token

---

## 📁 Estructura de Archivos

```
frnt/src/app/
├── components/
│   ├── login/
│   │   ├── login.component.ts          # Componente de login
│   │   ├── login.component.html         # Template del login
│   │   └── login.component.css          # Estilos del login
│   └── header/
│       ├── header.component.ts          # Header con info del usuario
│       ├── header.component.html        # Template del header
│       └── header.component.css          # Estilos del header
├── services/
│   └── auth.service.ts                  # Servicio de autenticación
├── guards/
│   └── auth.guard.ts                    # Guard para proteger rutas
├── interceptors/
│   └── auth.interceptor.ts              # Interceptor HTTP para JWT
└── app.routes.ts                         # Configuración de rutas
```

---

## 🔑 Componentes Principales

### 1. AuthService (`services/auth.service.ts`)

Servicio central de autenticación que maneja toda la lógica de autenticación.

#### Métodos principales:

```typescript
// Iniciar sesión
login(username: string, password: string): Observable<LoginResponse>

// Cerrar sesión
logout(): void

// Verificar si el usuario está autenticado
isLoggedIn(): boolean

// Obtener información del usuario actual
getUser(): User | null

// Verificar si el usuario tiene un rol específico
hasRole(role: string): boolean

// Verificar si el usuario tiene un permiso
can(permission: string): boolean

// Obtener el token JWT
getToken(): string | null

// Verificar si es administrador
get isAdmin(): boolean

// Obtener el ID de la plaza del usuario
get plazaId(): number | undefined
```

#### Características:

- **Storage**: Almacena el token y datos del usuario en `localStorage`
- **Validación de expiración**: Verifica que el token no haya expirado usando `jwt-decode`
- **BehaviorSubject**: Expone un Observable para reaccionar a cambios en el estado del usuario
- **Mapeo de roles a permisos**: Convierte roles del backend a permisos de la app

---

### 2. LoginComponent (`components/login/login.component.ts`)

Componente de formulario de login con validación reactiva.

#### Características:

```typescript
// Formulario con validaciones
loginForm = fb.group({
  username: ['', [Validators.required, Validators.minLength(3)]],
  password: ['', [Validators.required, Validators.minLength(6)]]
})

// Manejo de errores
hasError(fieldName: string): boolean
getErrorMessage(fieldName: string): string

// Estado de carga
isLoading: boolean = false
```

#### Template:

```html
<form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
  <input formControlName="username" />
  <input formControlName="password" type="password" />
  <button type="submit" [disabled]="isLoading">Iniciar Sesión</button>
</form>
```

---

### 3. HeaderComponent (`components/header/header.component.ts`)

Header que muestra información del usuario y permite hacer logout.

#### Características:

- Muestra el nombre completo del usuario
- Muestra roles y plaza asignada
- Botón de logout
- Navegación entre secciones
- Se suscribe a cambios en el usuario mediante `authService.user$`

---

### 4. AuthInterceptor (`interceptors/auth.interceptor.ts`)

Interceptor HTTP que añade automáticamente el token JWT a todas las peticiones.

```typescript
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = this.authService.getToken();
  
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next.handle(cloned);
  }
  
  return next.handle(req);
}
```

#### Registro en app.config.ts:

```typescript
providers: [
  {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthInterceptor,
    multi: true
  }
]
```

---

### 5. AuthGuard (`guards/auth.guard.ts`)

Guard funcional (Angular 17+) que protege las rutas.

```typescript
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};
```

#### Uso en rutas:

```typescript
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'plaza', component: PlazaComponent, canActivate: [authGuard] },
  // ...
];
```

---

## 🎨 Ejemplos de Uso

### 1. Verificar Roles en el Template

```html
<!-- Mostrar botón solo para ADMIN -->
<button *ngIf="authService.getUser()?.roles.includes('ADMIN')">
  Panel de Administración
</button>

<!-- Usar el método hasRole -->
<div *ngIf="authService.hasRole('MANAGER')">
  Contenido solo para Managers
</div>
```

### 2. Verificar Permisos en el Código

```typescript
export class MiComponente implements OnInit {
  canEdit = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Verificar si el usuario puede editar
    this.canEdit = this.authService.can('plaza:write');
  }
}
```

### 3. Acceder a Información del Usuario

```typescript
export class MiComponente implements OnInit {
  user: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    
    if (this.user) {
      console.log('Nombre:', this.user.fullName);
      console.log('Roles:', this.user.roles);
      console.log('Plaza:', this.user.plazaName);
    }
  }
}
```

### 4. Escuchar Cambios en el Usuario

```typescript
export class MiComponente implements OnInit, OnDestroy {
  private userSubscription?: Subscription;

  ngOnInit(): void {
    this.userSubscription = this.authService.user$.subscribe(user => {
      if (user) {
        console.log('Usuario autenticado:', user.fullName);
      } else {
        console.log('Usuario deslogueado');
      }
    });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }
}
```

---

## 🔐 Flujo de Autenticación

1. **Login**:
   - Usuario ingresa credenciales en `LoginComponent`
   - Se llama a `authService.login(username, password)`
   - Backend valida y retorna JWT
   - Token y datos del usuario se guardan en localStorage
   - Usuario es redirigido a `/plaza`

2. **Peticiones HTTP**:
   - `AuthInterceptor` intercepta todas las peticiones
   - Añade el header `Authorization: Bearer <token>`
   - Backend valida el token en cada request

3. **Protección de Rutas**:
   - `authGuard` verifica si el usuario está autenticado
   - Si no lo está, redirige a `/login`
   - Si está autenticado, permite el acceso

4. **Logout**:
   - Usuario hace click en "Cerrar Sesión"
   - Se llama a `authService.logout()`
   - Se limpia localStorage
   - Usuario es redirigido a `/login`

---

## 📦 Dependencias

```json
{
  "dependencies": {
    "@angular/core": "^20.3.0",
    "@angular/forms": "^20.3.0",
    "@angular/router": "^20.3.0",
    "jwt-decode": "^4.0.0",
    "rxjs": "~7.8.0"
  }
}
```

### Instalación:

```bash
npm install jwt-decode
```

---

## 🔧 Configuración del Backend

El backend debe proporcionar un endpoint de login que retorne:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "id": 1,
  "username": "manager1",
  "email": "manager@example.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "fullName": "Juan Pérez",
  "plazaId": 1,
  "plazaName": "Plaza Central",
  "roles": ["MANAGER"]
}
```

El JWT debe incluir en su payload:
```json
{
  "sub": "manager1",
  "roles": ["MANAGER"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

---

## 🎯 Roles y Permisos

### Roles del Backend:

- `ADMIN`: Acceso total al sistema
- `MANAGER`: Gestión de plazas y usuarios
- `EMPLOYEE_GENERAL`: Acceso a boletines
- `EMPLOYEE_SECURITY`: Acceso a seguridad
- `EMPLOYEE_PARKING`: Acceso a parqueadero

### Mapeo de Roles a Permisos:

```typescript
MANAGER/ADMIN → ['plaza:read', 'plaza:write', 'user:read', 'user:write']
EMPLOYEE_GENERAL → ['bulletin:read', 'bulletin:write']
EMPLOYEE_SECURITY → ['security:read', 'security:write']
EMPLOYEE_PARKING → ['parking:read', 'parking:write']
```

---

## 🚀 Uso en Producción

### Mejoras recomendadas:

1. **Refresh Token**: Implementar refresh de tokens para mejorar la experiencia
2. **Auto-logout**: Cerrar sesión automáticamente cuando el token expire
3. **Handle 401**: Interceptor que maneje errores 401 y redirija a login
4. **Biometric Auth**: Añadir autenticación biométrica (escaner de huellas)
5. **2FA**: Implementar autenticación de dos factores
6. **Session Timeout**: Implementar timeout de sesión por inactividad

### Seguridad:

- ✅ Token almacenado de forma segura en localStorage
- ✅ Validación de expiración del token
- ✅ HTTPS obligatorio en producción
- ✅ CSRF tokens en el backend
- ✅ Sanitización de inputs

---

## 📊 Usuarios de Prueba

```
Manager:    manager1 / password123
Security:   security1 / password123
Parking:    parking1 / password123
```

---

## 📝 Notas Importantes

1. **Standalone Components**: Este proyecto usa componentes standalone de Angular 17+
2. **Reactive Forms**: Todos los formularios usan `ReactiveFormsModule`
3. **Observable Pattern**: Uso de RxJS Observables para manejo asíncrono
4. **Type Safety**: TypeScript estricto para mayor seguridad de tipos
5. **Responsive Design**: UI adaptada para móviles y tablets

---

## 🐛 Troubleshooting

### Error: "Cannot find module 'jwt-decode'"
```bash
npm install jwt-decode
```

### Error: "Token expired"
El sistema automáticamente limpia el localStorage y redirige a login

### Error: "CORS"
Asegúrate de que el backend tenga configurado CORS para permitir requests desde `http://localhost:4200`

---

## 📚 Referencias

- [Angular HttpClient](https://angular.io/api/common/http/HttpClient)
- [Angular Guards](https://angular.io/api/router/CanActivate)
- [Angular Interceptors](https://angular.io/api/common/http/HttpInterceptor)
- [JWT.io](https://jwt.io/)
- [jwt-decode npm](https://www.npmjs.com/package/jwt-decode)

