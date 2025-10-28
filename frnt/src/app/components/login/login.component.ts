import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Componente de login con formulario reactivo
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Si ya está autenticado, redirigir a home
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/plaza']);
    }

    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  /**
   * Maneja el submit del formulario de login
   */
  onSubmit(): void {
    if (this.loginForm.valid) {
      this.errorMessage = null;
      this.isLoading = true;

      const { username, password } = this.loginForm.value;

      this.authService.login(username, password).subscribe({
        next: (response) => {
          console.log('Login exitoso:', response);
          this.isLoading = false;
          // Redirigir al home después del login exitoso
          this.router.navigate(['/plaza']);
        },
        error: (error) => {
          console.error('Error en login:', error);
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Credenciales inválidas. Intente nuevamente.';
        }
      });
    } else {
      // Marcar todos los campos como touched para mostrar errores
      Object.keys(this.loginForm.controls).forEach((key) => {
        this.loginForm.get(key)?.markAsTouched();
      });
    }
  }

  /**
   * Verifica si un campo tiene errores
   */
  hasError(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  /**
   * Obtiene el mensaje de error para un campo
   */
  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (!field?.errors) return '';

    if (field.errors['required']) {
      return `${fieldName === 'username' ? 'Usuario' : 'Contraseña'} es requerido`;
    }
    if (field.errors['minlength']) {
      return `Debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
    }

    return '';
  }
}

