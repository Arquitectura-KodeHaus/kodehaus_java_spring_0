import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { PlazaService } from '../../services/plaza.service';

@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.css']
})
export class PagosComponent implements OnInit {
  pagos: any[] = [];
  showFactura = false;
  selectedPago: any | null = null;

  constructor(private plazaService: PlazaService) {}

  ngOnInit(): void {
    this.plazaService.getPagos().subscribe({
      next: (data: any) => this.pagos = data,
      error: (err: any) => console.error('Error cargando pagos', err)
    });
  }

  openFactura(pago: any) {
    this.selectedPago = pago;
    this.showFactura = true;
  }

  closeFactura() {
    this.showFactura = false;
    this.selectedPago = null;
  }

  descargarFactura() {
    if (!this.selectedPago) return;
    const contenido = `Factura Mock\n\nReferencia: ${this.selectedPago.referencia}\nConcepto: ${this.selectedPago.concepto}\nTipo: ${this.selectedPago.tipo}\nMonto: ${this.selectedPago.monto}\nFecha: ${this.selectedPago.fecha}\nEstado: ${this.selectedPago.estado}`;
    const blob = new Blob([contenido], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `factura_${this.selectedPago.referencia}.txt`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }

  pagarEnLinea() {
    if (!this.selectedPago) return;
    const w = window.open('', '_blank');
    if (w) {
      w.document.write(`
        <div style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; padding:24px;">
          <h2>Pasarela Mock</h2>
          <p>Vas a pagar la factura <strong>${this.selectedPago.referencia}</strong></p>
          <p>Monto: <strong>$${this.selectedPago.monto}</strong></p>
          <button onclick="document.body.innerHTML='<h3>Pago aprobado ✅</h3><p>Ref: ${this.selectedPago.referencia}</p>';" style="padding:10px 16px;border:none;border-radius:8px;background:#16a34a;color:white;font-weight:700;cursor:pointer;">Pagar ahora</button>
        </div>
      `);
    }
  }
}
