import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-parqueadero',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './parqueadero.component.html',
  styleUrls: ['./parqueadero.component.css']
})
export class ParqueaderoComponent implements OnInit {
  parqueadero: any[] = [];
  cuposTotales = 0;
  ocupados = 0;
  ocupacion = 0;
  vehiculosDentro: any[] = [];
  ingresosMensuales: { mes: string; ingresos: number }[] = [];
  tarifas: any = {};
  ingresosHoy = 0;
  maxIngresos = 0;

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getParqueadero().subscribe({
      next: (data: any) => {
        this.cuposTotales = data.cuposTotales || 0;
        this.ocupados = data.ocupados || 0;
        this.ocupacion = data.ocupacion || 0;
        this.parqueadero = data.vehiculos || [];
        this.vehiculosDentro = data.vehiculosDentro || [];
        this.ingresosMensuales = data.ingresosMensuales || [];
        this.tarifas = data.tarifas || {};
        this.ingresosHoy = data.ingresosHoy || 0;
        this.maxIngresos = this.ingresosMensuales.reduce((m, x) => Math.max(m, x.ingresos), 0);
      },
      error: (err: any) => console.error('Error cargando parqueadero', err)
    });
  }
}
