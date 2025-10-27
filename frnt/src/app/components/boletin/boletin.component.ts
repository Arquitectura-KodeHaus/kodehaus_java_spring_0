import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-boletin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './boletin.component.html',
  styleUrls: ['./boletin.component.css']
})
export class BoletinComponent implements OnInit {
  boletin: any[] = [];

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getBoletin().subscribe({
      next: (data: any) => this.boletin = data,
      error: (err: any) => console.error('Error cargando boletín', err)
    });
  }
}
