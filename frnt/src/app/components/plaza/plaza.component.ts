import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PlazaDto, PlazaService } from '../../services/plaza.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-plaza',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './plaza.component.html',
  styleUrls: ['./plaza.component.css']
})
export class PlazaComponent implements OnInit {
  plazas: PlazaDto[] = [];
  form!: FormGroup;
  selected?: PlazaDto;
  isCreating = false;

  canEdit = false;

  constructor(private plazaService: PlazaService, private fb: FormBuilder, private auth: AuthService) {}

  ngOnInit(): void {
    this.canEdit = this.auth.can('plaza:write');

    this.form = this.fb.group({
      id: [null],
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      address: ['', [Validators.required]],
      phoneNumber: ['', [Validators.required]],
      email: ['', []],
      openingHours: ['', [Validators.required]],
      closingHours: ['', [Validators.required]]
    });

    if (!this.canEdit) {
      this.form.disable({ emitEvent: false });
    }

    // In a single-plaza system, get the user's plaza from login response
    // If we have plazaId, fetch just that plaza; otherwise fetch all (which will return only user's plaza)
    const user = this.auth.getUser();
    if (user && user.plazaId) {
      // Fetch only the user's plaza
      this.plazaService.getPlaza(user.plazaId).subscribe({
        next: (plaza: PlazaDto) => {
          this.plazas = [plaza];
          this.startEdit(plaza);
        },
        error: (err: any) => {
          console.error('Error cargando plaza del usuario', err);
          // Fallback: try to get all plazas (backend will return only user's plaza)
          this.loadUserPlaza();
        }
      });
    } else {
      // No plazaId in user data, try to fetch (backend will return only user's plaza)
      this.loadUserPlaza();
    }
  }

  private loadUserPlaza(): void {
    this.plazaService.getPlazas().subscribe({
      next: (data: PlazaDto[]) => {
        this.plazas = data;
        if (this.plazas.length > 0) {
          this.startEdit(this.plazas[0]);
        }
      },
      error: (err: any) => {
        console.error('Error cargando plazas', err);
        // Don't show error to user if it's a 403 - they just don't have permission
        // This is a single-plaza system, so if they can't see their plaza, something is wrong
      }
    });
  }

  startEdit(plaza?: PlazaDto) {
    this.isCreating = false;
    this.selected = plaza ?? undefined;
    if (plaza) {
      this.form.patchValue(plaza);
    } else {
      this.form.reset({ id: null, name: '', description: '', address: '', phoneNumber: '', email: '', openingHours: '', closingHours: '' });
    }
  }

  newPlaza() {
    this.isCreating = true;
    this.selected = undefined;
    this.form.reset({ id: null, name: '', description: '', address: '', phoneNumber: '', email: '', openingHours: '', closingHours: '' });
    if (!this.canEdit) {
      this.form.disable({ emitEvent: false });
    }
  }

  save() {
    const value: PlazaDto = this.form.value;
    if (this.isCreating || !value.id) {
      this.plazaService.createPlaza(value).subscribe({
        next: (created) => {
          this.plazas = [created, ...this.plazas];
          this.startEdit(created);
        },
        error: (e) => console.error('Error creando plaza', e)
      });
    } else {
      this.plazaService.updatePlaza(value.id!, value).subscribe({
        next: (updated) => {
          const idx = this.plazas.findIndex(p => p.id === updated.id);
          if (idx >= 0) this.plazas[idx] = updated;
          this.startEdit(updated);
        },
        error: (e) => console.error('Error actualizando plaza', e)
      });
    }
  }

  select(plaza: PlazaDto) {
    this.startEdit(plaza);
  }

  cancel() {
    if (this.selected) {
      this.startEdit(this.selected);
    } else if (this.plazas.length > 0) {
      this.startEdit(this.plazas[0]);
    } else {
      this.newPlaza();
    }
  }
}
