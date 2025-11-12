import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StoreService, StoreDto } from '../../services/store.service';
import { AuthService } from '../../services/auth.service';
import { CreateStoreComponent } from './create-store.component';

@Component({
  selector: 'app-locales',
  standalone: true,
  imports: [CommonModule, RouterModule, CreateStoreComponent],
  templateUrl: './locales.component.html',
  styleUrls: ['./locales.component.css']
})
export class LocalesComponent implements OnInit {
  stores: StoreDto[] = [];
  isLoading = false;
  errorMessage = '';
  showCreateForm = false;

  constructor(
    private storeService: StoreService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadStores();
  }

  loadStores(): void {
    this.isLoading = true;
    this.storeService.getStores().subscribe({
      next: (data: StoreDto[]) => {
        this.stores = data;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error cargando stores', err);
        this.errorMessage = 'Error al cargar los locales';
        this.isLoading = false;
      }
    });
  }

  canCreateStore(): boolean {
    return this.authService.hasRole('MANAGER') || this.authService.hasRole('ADMIN');
  }

  deleteStore(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar este local?')) {
      this.storeService.deleteStore(id).subscribe({
        next: () => {
          this.loadStores();
        },
        error: (err: any) => {
          console.error('Error eliminando store', err);
          this.errorMessage = 'Error al eliminar el local';
        }
      });
    }
  }

  onStoreCreated(): void {
    this.showCreateForm = false;
    this.loadStores();
  }
}
