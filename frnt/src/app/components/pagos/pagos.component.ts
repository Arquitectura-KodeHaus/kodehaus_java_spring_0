import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.css']
})
export class PagosComponent implements OnInit {
  pagos: any[] = [];

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getPagos().subscribe({
      next: (data: any) => this.pagos = data,
      error: (err: any) => console.error('Error cargando pagos', err)
    });
  }
}
