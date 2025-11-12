import { Component, Input, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { StoreService } from '../../services/store.service';

@Component({
  selector: 'app-create-store-owner',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h2>Crear Dueño del Local</h2>
        <p class="subtitle" *ngIf="currentStoreId">Store ID: {{ currentStoreId }}</p>
      </div>
      
      <div class="create-store-owner-form">
        <form [formGroup]="ownerForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="username">Usuario *</label>
          <input 
            type="text" 
            id="username" 
            formControlName="username" 
            class="form-control"
            [class.error]="ownerForm.get('username')?.invalid && ownerForm.get('username')?.touched">
          <div *ngIf="ownerForm.get('username')?.invalid && ownerForm.get('username')?.touched" class="error-message">
            El usuario es requerido (mínimo 3 caracteres)
          </div>
        </div>

        <div class="form-group">
          <label for="email">Email *</label>
          <input 
            type="email" 
            id="email" 
            formControlName="email" 
            class="form-control"
            [class.error]="ownerForm.get('email')?.invalid && ownerForm.get('email')?.touched">
          <div *ngIf="ownerForm.get('email')?.invalid && ownerForm.get('email')?.touched" class="error-message">
            Email inválido
          </div>
        </div>

        <div class="form-group">
          <label for="password">Contraseña *</label>
          <input 
            type="password" 
            id="password" 
            formControlName="password" 
            class="form-control"
            [class.error]="ownerForm.get('password')?.invalid && ownerForm.get('password')?.touched">
          <div *ngIf="ownerForm.get('password')?.invalid && ownerForm.get('password')?.touched" class="error-message">
            La contraseña es requerida (mínimo 6 caracteres)
          </div>
        </div>

        <div class="form-group">
          <label for="firstName">Nombre *</label>
          <input 
            type="text" 
            id="firstName" 
            formControlName="firstName" 
            class="form-control"
            [class.error]="ownerForm.get('firstName')?.invalid && ownerForm.get('firstName')?.touched">
          <div *ngIf="ownerForm.get('firstName')?.invalid && ownerForm.get('firstName')?.touched" class="error-message">
            El nombre es requerido
          </div>
        </div>

        <div class="form-group">
          <label for="lastName">Apellido *</label>
          <input 
            type="text" 
            id="lastName" 
            formControlName="lastName" 
            class="form-control"
            [class.error]="ownerForm.get('lastName')?.invalid && ownerForm.get('lastName')?.touched">
          <div *ngIf="ownerForm.get('lastName')?.invalid && ownerForm.get('lastName')?.touched" class="error-message">
            El apellido es requerido
          </div>
        </div>

        <div class="form-group">
          <label for="phoneNumber">Teléfono</label>
          <input 
            type="tel" 
            id="phoneNumber" 
            formControlName="phoneNumber" 
            class="form-control">
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" [disabled]="ownerForm.invalid || isLoading">
            {{ isLoading ? 'Creando...' : 'Crear Dueño' }}
          </button>
          <button type="button" class="btn-secondary" (click)="onCancel()" [disabled]="isLoading">
            Cancelar
          </button>
        </div>

          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>
          <div *ngIf="successMessage" class="success-message">
            {{ successMessage }}
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .page-container {
      max-width: 800px;
      margin: 2rem auto;
      padding: 0 1rem;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h2 {
      margin: 0 0 0.5rem 0;
    }

    .subtitle {
      color: #6c757d;
      margin: 0;
    }

    .create-store-owner-form {
      padding: 2rem;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
    }

    .form-control {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }

    .form-control.error {
      border-color: #dc3545;
    }

    .error-message {
      color: #dc3545;
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
    }

    .btn-primary, .btn-secondary {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
    }

    .btn-primary {
      background: #007bff;
      color: white;
    }

    .btn-primary:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    .btn-secondary {
      background: #6c757d;
      color: white;
    }

    .success-message {
      color: #28a745;
      font-size: 0.875rem;
      margin-top: 1rem;
      padding: 0.75rem;
      background: #d4edda;
      border-radius: 4px;
    }
  `]
})
export class CreateStoreOwnerComponent implements OnInit {
  @Input() storeId?: number;
  @Output() ownerCreated = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  ownerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  currentStoreId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private storeService: StoreService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.ownerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      phoneNumber: ['']
    });
  }

  ngOnInit(): void {
    // Get storeId from route if not provided as input
    const storeIdParam = this.route.snapshot.paramMap.get('storeId');
    if (storeIdParam) {
      this.currentStoreId = +storeIdParam;
    } else if (this.storeId) {
      this.currentStoreId = this.storeId;
    }
    
    if (!this.currentStoreId) {
      this.errorMessage = 'Store ID no proporcionado';
    }
  }

  onSubmit(): void {
    if (!this.currentStoreId) {
      this.errorMessage = 'Store ID no proporcionado';
      return;
    }
    
    if (this.ownerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.storeService.createStoreOwner(this.currentStoreId, this.ownerForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.successMessage = 'Dueño del local creado exitosamente';
          this.ownerCreated.emit();
          this.ownerForm.reset();
          
          // If standalone page (accessed via route), redirect after 2 seconds
          if (!this.storeId) {
            setTimeout(() => {
              this.router.navigate(['/locales']);
            }, 2000);
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || error.message || 'Error al crear el dueño del local';
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.ownerForm.controls).forEach(key => {
        this.ownerForm.get(key)?.markAsTouched();
      });
    }
  }

  onCancel(): void {
    if (this.cancel.observers.length > 0) {
      this.cancel.emit();
    } else {
      this.router.navigate(['/locales']);
    }
  }
}

