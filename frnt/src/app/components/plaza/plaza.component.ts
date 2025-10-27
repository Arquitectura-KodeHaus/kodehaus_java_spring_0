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

  canEdit = false;

  constructor(private plazaService: PlazaService, private fb: FormBuilder, private auth: AuthService) {}

  ngOnInit(): void {
    this.canEdit = this.auth.can('plaza:write');

    this.form = this.fb.group({
      id: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      direccion: ['', [Validators.required]],
      contacto: ['', [Validators.required]],
      horario: ['', [Validators.required]]
    });

    if (!this.canEdit) {
      this.form.disable({ emitEvent: false });
    }

    const plazaId = this.auth.plazaId;
    if (plazaId) {
      this.plazaService.getPlaza(plazaId).subscribe({
        next: (plaza) => {
          this.plazas = [plaza];
          this.startEdit(plaza);
        },
        error: (err) => console.error('Error cargando plaza del usuario', err)
      });
    } else {
      this.plazaService.getPlazas().subscribe({
        next: (data: PlazaDto[]) => {
          this.plazas = data;
          if (this.plazas.length > 0) {
            this.startEdit(this.plazas[0]);
          }
        },
        error: (err: any) => console.error('Error cargando plazas', err)
      });
    }
  }

  startEdit(plaza?: PlazaDto) {
    this.selected = plaza ?? undefined;
    if (plaza) {
      this.form.patchValue(plaza);
    } else {
      this.form.reset({ id: null, nombre: '', direccion: '', contacto: '', horario: '' });
    }
  }

  save() {
    const value: PlazaDto = this.form.value;
    if (value.id) {
      this.plazaService.updatePlaza(value.id, value).subscribe({
        next: (updated) => {
          const idx = this.plazas.findIndex(p => p.id === updated.id);
          if (idx >= 0) this.plazas[idx] = updated;
          this.startEdit(updated);
        },
        error: (e) => console.error('Error actualizando plaza', e)
      });
    } else {
      this.plazaService.createPlaza(value).subscribe({
        next: (created) => {
          this.plazas = [created, ...this.plazas];
          this.startEdit(created);
        },
        error: (e) => console.error('Error creando plaza', e)
      });
    }
  }

  select(plaza: PlazaDto) {
    this.startEdit(plaza);
  }
}
