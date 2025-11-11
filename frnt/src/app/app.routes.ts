import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { PlazaComponent } from './components/plaza/plaza.component';
import { BoletinComponent } from './components/boletin/boletin.component';
import { LocalesComponent } from './components/locales/locales.component';
import { PagosComponent } from './components/pagos/pagos.component';
import { ParqueaderoComponent } from './components/parqueadero/parqueadero.component';
import { UsuariosComponent } from './components/usuarios/usuarios.component';
import { CreateStoreOwnerComponent } from './components/locales/create-store-owner.component';
import { ModulesComponent } from './components/modules/modules.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Ruta pública de login
  { path: 'login', component: LoginComponent },
  
  // Redirigir root a plaza
  { path: '', redirectTo: 'plaza', pathMatch: 'full' },
  
  // Rutas protegidas con authGuard
  { path: 'plaza', component: PlazaComponent, canActivate: [authGuard] },
  { path: 'boletin', component: BoletinComponent, canActivate: [authGuard] },
  { path: 'locales', component: LocalesComponent, canActivate: [authGuard] },
  { path: 'stores/:storeId/owner', component: CreateStoreOwnerComponent, canActivate: [authGuard] },
  { path: 'pagos', component: PagosComponent, canActivate: [authGuard] },
  { path: 'parqueadero', component: ParqueaderoComponent, canActivate: [authGuard] },
  { path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard] },
  { path: 'modules', component: ModulesComponent, canActivate: [authGuard] },
  
  // Ruta catch-all (404)
  { path: '**', redirectTo: 'plaza' }
];
