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

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getParqueadero().subscribe({
      next: (data: any) => this.parqueadero = data,
      error: (err: any) => console.error('Error cargando parqueadero', err)
    });
  }
}
