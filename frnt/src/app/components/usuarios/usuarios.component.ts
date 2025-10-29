import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UserService, UserRequest, UserResponse, Role } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.css']
})
export class UsuariosComponent implements OnInit {
  users: UserResponse[] = [];
  roles: Role[] = [];
  plazas: any[] = [];
  userForm!: FormGroup;
  isEditing = false;
  editingUserId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private plazaService: PlazaService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadData();
  }

  private initializeForm(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      phoneNumber: [''],
      plazaId: [null, [Validators.required]],
      roleId: [null, [Validators.required]]
    });
  }

  private loadData(): void {
    this.isLoading = true;
    
    // Cargar usuarios (excluir solo managers)
    this.userService.getUsers().subscribe({
      next: (users) => {
        // Filtrar solo managers, mostrar el resto de usuarios
        this.users = users.filter(user => {
          // Verificar si el usuario tiene rol MANAGER
          const hasManagerRole = user.roles && user.roles.some((role: Role) => role.name === 'MANAGER');
          return !hasManagerRole;
        });
      },
      error: (error) => {
        console.error('Error cargando usuarios:', error);
        this.errorMessage = 'Error al cargar usuarios';
      }
    });

    // Cargar roles (excluir MANAGER)
    this.userService.getRoles().subscribe({
      next: (roles) => {
        this.roles = roles.filter(role => role.isActive && role.name !== 'MANAGER');
      },
      error: (error) => {
        console.error('Error cargando roles:', error);
        this.errorMessage = 'Error al cargar roles';
      }
    });

    // Cargar plazas
    this.plazaService.getPlazas().subscribe({
      next: (plazas) => {
        this.plazas = plazas.filter(plaza => plaza.isActive);
      },
      error: (error) => {
        console.error('Error cargando plazas:', error);
        this.errorMessage = 'Error al cargar plazas';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      const formValue = this.userForm.value;
      const userRequest: UserRequest = {
        username: formValue.username,
        email: formValue.email,
        password: formValue.password,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        phoneNumber: formValue.phoneNumber || undefined,
        plazaId: formValue.plazaId,
        roleIds: [formValue.roleId] // Convertir a array con un solo elemento
      };

      if (this.isEditing && this.editingUserId) {
        // Actualizar usuario existente
        this.userService.updateUser(this.editingUserId, userRequest).subscribe({
          next: (updatedUser) => {
            const index = this.users.findIndex(u => u.id === updatedUser.id);
            if (index !== -1) {
              this.users[index] = updatedUser;
            }
            this.successMessage = 'Usuario actualizado exitosamente';
            this.resetForm();
          },
          error: (error) => {
            console.error('Error actualizando usuario:', error);
            this.errorMessage = error.error?.message || 'Error al actualizar usuario';
            this.isLoading = false;
          },
          complete: () => {
            this.isLoading = false;
          }
        });
      } else {
        // Crear nuevo usuario
        this.userService.createUser(userRequest).subscribe({
          next: (newUser) => {
            this.users.push(newUser);
            this.successMessage = 'Usuario creado exitosamente';
            this.resetForm();
          },
          error: (error) => {
            console.error('Error creando usuario:', error);
            this.errorMessage = error.error?.message || 'Error al crear usuario';
            this.isLoading = false;
          },
          complete: () => {
            this.isLoading = false;
          }
        });
      }
    } else {
      // Marcar todos los campos como touched para mostrar errores
      Object.keys(this.userForm.controls).forEach((key) => {
        this.userForm.get(key)?.markAsTouched();
      });
    }
  }

  editUser(user: UserResponse): void {
    this.isEditing = true;
    this.editingUserId = user.id;
    
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      password: '', // No mostrar contraseña existente
      firstName: user.firstName,
      lastName: user.lastName,
      phoneNumber: user.phoneNumber || '',
      plazaId: user.plazaId,
      roleId: user.roles.length > 0 ? user.roles[0].id : null // Solo el primer rol
    });
  }

  deleteUser(user: UserResponse): void {
    if (confirm(`¿Está seguro de que desea eliminar al usuario ${user.fullName}?`)) {
      this.isLoading = true;
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== user.id);
          this.successMessage = 'Usuario eliminado exitosamente';
        },
        error: (error) => {
          console.error('Error eliminando usuario:', error);
          this.errorMessage = error.error?.message || 'Error al eliminar usuario';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  resetForm(): void {
    this.userForm.reset();
    this.isEditing = false;
    this.editingUserId = null;
    this.errorMessage = null;
    this.successMessage = null;
  }

  hasError(fieldName: string): boolean {
    const field = this.userForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.userForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) {
        return `${this.getFieldLabel(fieldName)} es requerido`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldLabel(fieldName)} debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
      }
      if (field.errors['maxlength']) {
        return `${this.getFieldLabel(fieldName)} no puede tener más de ${field.errors['maxlength'].requiredLength} caracteres`;
      }
      if (field.errors['email']) {
        return 'Email inválido';
      }
    }
    return '';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      username: 'Usuario',
      email: 'Email',
      password: 'Contraseña',
      firstName: 'Nombre',
      lastName: 'Apellido',
      phoneNumber: 'Teléfono',
      plazaId: 'Plaza',
      roleId: 'Rol'
    };
    return labels[fieldName] || fieldName;
  }

  canManageUsers(): boolean {
    return this.authService.hasRole('MANAGER') || this.authService.hasRole('ADMIN');
  }

  getRoleDisplayName(role: Role | string): string {
    // Si es un objeto Role, extraer el nombre
    const roleName = typeof role === 'string' ? role : role.name;
    
    const roleMap: { [key: string]: string } = {
      'EMPLOYEE_SECURITY': 'Personal de Seguridad',
      'EMPLOYEE_PARKING': 'Personal de Parqueaderos',
      'EMPLOYEE_GENERAL': 'Empleado General',
      'MANAGER': 'Manager',
      'ADMIN': 'Administrador'
    };
    return roleMap[roleName] || roleName;
  }

  onRoleChange(event: any, roleId: number): void {
    this.userForm.get('roleId')?.setValue(roleId);
    this.userForm.get('roleId')?.markAsTouched();
  }

  isRoleSelected(roleId: number): boolean {
    const selectedRoleId = this.userForm.get('roleId')?.value;
    return selectedRoleId === roleId;
  }
}
