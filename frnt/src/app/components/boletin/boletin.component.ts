import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BulletinService, BulletinDto, BulletinResponseDto } from '../../services/bulletin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-boletin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './boletin.component.html',
  styleUrls: ['./boletin.component.css']
})
export class BoletinComponent implements OnInit {
  bulletins: BulletinResponseDto[] = [];
  bulletinForm!: FormGroup;
  showCreateForm = false;
  isEditing = false;
  editingBulletinId: number | null = null;
  canCreateBulletin = false;
  selectedFile: File | null = null;

  constructor(
    private bulletinService: BulletinService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.canCreateBulletin = this.authService.hasRole('MANAGER');

    this.initForm();
    this.loadBulletins();
  }

  initForm(): void {
    this.bulletinForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      publicationDate: [new Date().toISOString().split('T')[0]],
      file: [null, Validators.required]
    });
  }

  loadBulletins(): void {
    this.bulletinService.getTodaysBulletins().subscribe({
      next: (data) => {
        this.bulletins = data;
      },
      error: (err) => console.error('Error cargando boletines', err)
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.isEditing = false;
    this.editingBulletinId = null;
    this.initForm();
  }

  startEdit(bulletin: BulletinResponseDto): void {
    this.isEditing = true;
    this.editingBulletinId = bulletin.id;
    this.showCreateForm = true;
    
    this.bulletinForm.patchValue({
      title: bulletin.title,
      publicationDate: bulletin.publicationDate
    });
  }

  onSubmit(): void {
    if (this.bulletinForm.valid) {
      const formData = this.bulletinForm.value;
      const plazaId = this.authService.plazaId;

      if (!plazaId) {
        console.error('No se pudo obtener el ID de la plaza');
        return;
      }

      // Check if file is required
      if (!this.selectedFile) {
        alert('Por favor, selecciona un archivo para el boletín.');
        this.bulletinForm.get('file')?.markAsTouched();
        return;
      }

      if (this.isEditing && this.editingBulletinId) {
        // For editing, use regular JSON endpoint (no content field)
        const bulletinData: BulletinDto = {
          title: formData.title,
          publicationDate: formData.publicationDate,
          plazaId: plazaId
        };

        this.bulletinService.updateBulletin(this.editingBulletinId, bulletinData).subscribe({
          next: (updatedBulletin) => {
            const index = this.bulletins.findIndex(b => b.id === updatedBulletin.id);
            if (index >= 0) {
              this.bulletins[index] = updatedBulletin;
            }
            this.resetForm();
          },
          error: (err) => console.error('Error actualizando boletín', err)
        });
      } else {
        // For creating, use multipart form data for file upload (file is required)
        const formDataToSend = new FormData();
        formDataToSend.append('title', formData.title);
        formDataToSend.append('publicationDate', formData.publicationDate);
        formDataToSend.append('plazaId', plazaId.toString());
        formDataToSend.append('file', this.selectedFile);

        console.log('Enviando boletín con:', {
          title: formData.title,
          publicationDate: formData.publicationDate,
          plazaId: plazaId,
          fileName: this.selectedFile?.name,
          fileSize: this.selectedFile?.size
        });

        this.bulletinService.createBulletinWithFile(formDataToSend).subscribe({
          next: (newBulletin) => {
            console.log('Boletín creado exitosamente:', newBulletin);
            this.bulletins.unshift(newBulletin);
            this.resetForm();
          },
          error: (err) => {
            console.error('Error creando boletín con archivo:', err);
            console.error('Status:', err.status);
            console.error('Status text:', err.statusText);
            console.error('Error object:', err.error);
            console.error('Full error:', JSON.stringify(err.error, null, 2));
            
            let errorMsg = 'Error al crear el boletín. ';
            if (err.error) {
              if (err.error.message) {
                errorMsg += err.error.message;
              } else if (typeof err.error === 'string') {
                errorMsg += err.error;
              }
            } else if (err.message) {
              errorMsg += err.message;
            }
            
            // Mostrar mensaje más detallado
            console.error('Mensaje de error completo:', errorMsg);
            alert(errorMsg);
          }
        });
      }
    } else {
      // Marcar todos los campos como touched para mostrar errores
      Object.keys(this.bulletinForm.controls).forEach(key => {
        this.bulletinForm.get(key)?.markAsTouched();
      });
      
      if (!this.selectedFile) {
        this.bulletinForm.get('file')?.setErrors({ required: true });
      }
    }
  }

  deleteBulletin(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este boletín?')) {
      this.bulletinService.deleteBulletin(id).subscribe({
        next: () => {
          this.bulletins = this.bulletins.filter(b => b.id !== id);
        },
        error: (err) => console.error('Error eliminando boletín', err)
      });
    }
  }

  resetForm(): void {
    this.showCreateForm = false;
    this.isEditing = false;
    this.editingBulletinId = null;
    this.selectedFile = null;
    this.initForm();
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Check file size (10MB limit)
      const maxSize = 10 * 1024 * 1024; // 10MB
      if (file.size > maxSize) {
        alert('El archivo es demasiado grande. El tamaño máximo permitido es 10MB.');
        event.target.value = '';
        this.selectedFile = null;
        this.bulletinForm.get('file')?.setValue(null);
        return;
      }
      
      // Check file type
      const allowedTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'text/plain',
        'image/jpeg',
        'image/jpg',
        'image/png',
        'image/gif'
      ];
      
      if (!allowedTypes.includes(file.type)) {
        alert('Tipo de archivo no permitido. Tipos permitidos: PDF, DOC, DOCX, XLS, XLSX, TXT, JPG, PNG, GIF');
        event.target.value = '';
        this.selectedFile = null;
        this.bulletinForm.get('file')?.setValue(null);
        return;
      }
      
      this.selectedFile = file;
      this.bulletinForm.get('file')?.setValue(file);
    }
  }

  removeFile(): void {
    this.selectedFile = null;
    this.bulletinForm.get('file')?.setValue(null);
    // Reset file input
    const fileInput = document.getElementById('file') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  downloadFile(bulletin: BulletinResponseDto): void {
    if (!bulletin.id || !bulletin.fileName) {
      alert('Archivo no disponible');
      return;
    }

    this.bulletinService.downloadBulletinFile(bulletin.id).subscribe({
      next: (blob: Blob) => {
        // Create a blob URL and trigger download
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = bulletin.fileName || 'archivo';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error descargando archivo:', err);
        alert('Error al descargar el archivo');
      }
    });
  }

  hasError(fieldName: string): boolean {
    const field = this.bulletinForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.bulletinForm.get(fieldName);
    if (!field?.errors) return '';

    if (field.errors['required']) {
      if (fieldName === 'title') return 'Título es requerido';
      if (fieldName === 'file') return 'Archivo es requerido';
      return `${fieldName} es requerido`;
    }
    if (field.errors['minlength']) {
      return `Debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
    }
    if (field.errors['maxlength']) {
      return `No puede exceder ${field.errors['maxlength'].requiredLength} caracteres`;
    }

    return '';
  }
}
