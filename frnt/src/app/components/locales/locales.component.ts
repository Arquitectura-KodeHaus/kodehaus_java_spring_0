import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-locales',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './locales.component.html',
  styleUrls: ['./locales.component.css']
})
export class LocalesComponent implements OnInit {
  locales: any[] = [];

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getLocales().subscribe({
      next: (data: any) => this.locales = data,
      error: (err: any) => console.error('Error cargando locales', err)
    });
  }
}
