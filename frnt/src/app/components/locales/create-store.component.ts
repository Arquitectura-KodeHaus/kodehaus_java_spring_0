import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StoreService } from '../../services/store.service';

@Component({
  selector: 'app-create-store',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="create-store-form">
      <h3>Crear Nuevo Local</h3>
      <form [formGroup]="storeForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="name">Nombre del Local *</label>
          <input 
            type="text" 
            id="name" 
            formControlName="name" 
            class="form-control"
            [class.error]="storeForm.get('name')?.invalid && storeForm.get('name')?.touched">
          <div *ngIf="storeForm.get('name')?.invalid && storeForm.get('name')?.touched" class="error-message">
            El nombre es requerido
          </div>
        </div>

        <div class="form-group">
          <label for="description">Descripción</label>
          <textarea 
            id="description" 
            formControlName="description" 
            class="form-control"
            rows="3">
          </textarea>
        </div>

        <div class="form-group">
          <label for="ownerName">Nombre del Propietario</label>
          <input 
            type="text" 
            id="ownerName" 
            formControlName="ownerName" 
            class="form-control">
        </div>

        <div class="form-group">
          <label for="phoneNumber">Teléfono</label>
          <input 
            type="tel" 
            id="phoneNumber" 
            formControlName="phoneNumber" 
            class="form-control">
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input 
            type="email" 
            id="email" 
            formControlName="email" 
            class="form-control"
            [class.error]="storeForm.get('email')?.invalid && storeForm.get('email')?.touched">
          <div *ngIf="storeForm.get('email')?.invalid && storeForm.get('email')?.touched" class="error-message">
            Email inválido
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" [disabled]="storeForm.invalid || isLoading">
            {{ isLoading ? 'Creando...' : 'Crear Local' }}
          </button>
          <button type="button" class="btn-secondary" (click)="onCancel()" [disabled]="isLoading">
            Cancelar
          </button>
        </div>

        <div *ngIf="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </form>
    </div>
  `,
  styles: [`
    .create-store-form {
      padding: 2rem;
      background: white;
      border-radius: 8px;
      max-width: 600px;
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
  `]
})
export class CreateStoreComponent {
  @Output() storeCreated = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  storeForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private storeService: StoreService
  ) {
    this.storeForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      ownerName: [''],
      phoneNumber: [''],
      email: ['', [Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.storeForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.storeService.createStore(this.storeForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.storeCreated.emit();
          this.storeForm.reset();
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || error.message || 'Error al crear el local';
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.storeForm.controls).forEach(key => {
        this.storeForm.get(key)?.markAsTouched();
      });
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }
}

