import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-parqueadero',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parqueadero.component.html',
  styleUrls: ['./parqueadero.component.css']
})
export class ParqueaderoComponent implements OnInit {
  parqueadero: any[] = [];
  cuposTotales = 0;
  ocupados = 0;
  ocupacion = 0;

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getParqueadero().subscribe({
      next: (data: any) => {
        this.cuposTotales = data.cuposTotales || 0;
        this.ocupados = data.ocupados || 0;
        this.ocupacion = data.ocupacion || 0;
        this.parqueadero = data.vehiculos || [];
      },
      error: (err: any) => console.error('Error cargando parqueadero', err)
    });
  }
}
