import { Routes } from '@angular/router';
import { PlazaComponent } from './components/plaza/plaza.component';
import { BoletinComponent } from './components/boletin/boletin.component';
import { LocalesComponent } from './components/locales/locales.component';
import { PagosComponent } from './components/pagos/pagos.component';
import { ParqueaderoComponent } from './components/parqueadero/parqueadero.component';

export const routes: Routes = [
  { path: '', redirectTo: 'plaza', pathMatch: 'full' },
  { path: 'plaza', component: PlazaComponent },
  { path: 'boletin', component: BoletinComponent },
  { path: 'locales', component: LocalesComponent },
  { path: 'pagos', component: PagosComponent },
  { path: 'parqueadero', component: ParqueaderoComponent },
  { path: '**', redirectTo: 'plaza' }
];
