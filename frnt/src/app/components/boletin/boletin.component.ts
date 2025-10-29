import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PlazaService } from '../../services/plaza.service';
import { ProductService, ProductResponse } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import jsPDF from 'jspdf';

@Component({
  selector: 'app-boletin',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './boletin.component.html',
  styleUrls: ['./boletin.component.css']
})
export class BoletinComponent implements OnInit {
  boletin: any[] = [];
  products: ProductResponse[] = [];
  categories: string[] = [];
  productForm!: FormGroup;
  isEditing = false;
  editingProductId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  selectedCategory = '';
  showProductManagement = false;

  constructor(
    private plazaService: PlazaService,
    private productService: ProductService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadData();
  }

  private initializeForm(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      category: ['', [Validators.required, Validators.maxLength(50)]],
      unit: ['kg', [Validators.required, Validators.maxLength(20)]],
      price: [0, [Validators.required, Validators.min(0.01)]],
      isAvailable: [true]
    });
  }

  private loadData(): void {
    this.isLoading = true;
    
    // Cargar productos
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        // Agrupar productos por categoría para mostrar en boletín
        this.organizeProductsByCategory();
      },
      error: (error) => {
        console.error('Error cargando productos:', error);
        this.errorMessage = 'Error al cargar productos';
      }
    });

    // Cargar categorías
    this.productService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error cargando categorías:', error);
        this.errorMessage = 'Error al cargar categorías';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private organizeProductsByCategory(): void {
    // Agrupar productos por categoría para mostrar en la vista de boletín
    const groupedProducts: { [key: string]: ProductResponse[] } = {};
    
    this.products.forEach(product => {
      if (!groupedProducts[product.category]) {
        groupedProducts[product.category] = [];
      }
      groupedProducts[product.category].push(product);
    });

    // Convertir a formato de boletín
    this.boletin = Object.keys(groupedProducts).map(category => ({
      title: `Precios de ${category}`,
      category: category,
      products: groupedProducts[category],
      publicationDate: new Date()
    }));
  }

  toggleProductManagement(): void {
    this.showProductManagement = !this.showProductManagement;
    if (this.showProductManagement) {
      this.resetForm();
    }
  }

  onSubmit(): void {
    if (this.productForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      const formValue = this.productForm.value;
      const productRequest = {
        name: formValue.name,
        description: formValue.description || undefined,
        category: formValue.category,
        unit: formValue.unit,
        price: formValue.price,
        isAvailable: formValue.isAvailable
      };

      if (this.isEditing && this.editingProductId) {
        // Actualizar producto existente
        this.productService.updateProduct(this.editingProductId, productRequest).subscribe({
          next: (updatedProduct) => {
            const index = this.products.findIndex(p => p.id === updatedProduct.id);
            if (index !== -1) {
              this.products[index] = updatedProduct;
            }
            this.successMessage = 'Producto actualizado exitosamente';
            this.resetForm();
          },
          error: (error) => {
            console.error('Error actualizando producto:', error);
            this.errorMessage = error.error?.message || 'Error al actualizar producto';
            this.isLoading = false;
          },
          complete: () => {
            this.isLoading = false;
          }
        });
      } else {
        // Crear nuevo producto
        this.productService.createProduct(productRequest).subscribe({
          next: (newProduct) => {
            this.products.push(newProduct);
            this.successMessage = 'Producto creado exitosamente';
            this.resetForm();
          },
          error: (error) => {
            console.error('Error creando producto:', error);
            this.errorMessage = error.error?.message || 'Error al crear producto';
            this.isLoading = false;
          },
          complete: () => {
            this.isLoading = false;
          }
        });
      }
    } else {
      // Marcar todos los campos como touched para mostrar errores
      Object.keys(this.productForm.controls).forEach((key) => {
        this.productForm.get(key)?.markAsTouched();
      });
    }
  }

  editProduct(product: ProductResponse): void {
    this.isEditing = true;
    this.editingProductId = product.id;
    
    this.productForm.patchValue({
      name: product.name,
      description: product.description || '',
      category: product.category,
      unit: product.unit,
      price: product.price,
      isAvailable: product.isAvailable
    });
  }

  updatePrice(product: ProductResponse, event: any): void {
    const newPrice = parseFloat(event.target.value);
    if (newPrice > 0 && newPrice !== product.price) {
      this.isLoading = true;
      this.productService.updateProductPrice(product.id, newPrice).subscribe({
        next: (updatedProduct) => {
          const index = this.products.findIndex(p => p.id === updatedProduct.id);
          if (index !== -1) {
            this.products[index] = updatedProduct;
          }
          this.successMessage = 'Precio actualizado exitosamente';
        },
        error: (error) => {
          console.error('Error actualizando precio:', error);
          this.errorMessage = error.error?.message || 'Error al actualizar precio';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  deleteProduct(product: ProductResponse): void {
    if (confirm(`¿Está seguro de que desea eliminar el producto ${product.name}?`)) {
      this.isLoading = true;
      this.productService.deleteProduct(product.id).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.id !== product.id);
          this.successMessage = 'Producto eliminado exitosamente';
        },
        error: (error) => {
          console.error('Error eliminando producto:', error);
          this.errorMessage = error.error?.message || 'Error al eliminar producto';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  resetForm(): void {
    this.productForm.reset();
    this.productForm.patchValue({
      unit: 'kg',
      isAvailable: true
    });
    this.isEditing = false;
    this.editingProductId = null;
    this.errorMessage = null;
    this.successMessage = null;
  }

  filterByCategory(event: any): void {
    const category = event.target.value;
    this.selectedCategory = category;
    if (category === '') {
      this.loadData();
    } else {
      this.products = this.products.filter(p => p.category === category);
    }
  }

  filterBulletinByCategory(event: any): void {
    const category = event.target.value;
    if (category === '') {
      this.organizeProductsByCategory();
    } else {
      // Filtrar boletines por categoría específica
      const filteredProducts = this.products.filter(p => p.category === category);
      const groupedProducts: { [key: string]: ProductResponse[] } = {};
      
      filteredProducts.forEach(product => {
        if (!groupedProducts[product.category]) {
          groupedProducts[product.category] = [];
        }
        groupedProducts[product.category].push(product);
      });

      this.boletin = Object.keys(groupedProducts).map(cat => ({
        title: `Precios de ${cat}`,
        category: cat,
        products: groupedProducts[cat],
        publicationDate: new Date()
      }));
    }
  }

  hasError(fieldName: string): boolean {
    const field = this.productForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.productForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) {
        return `${this.getFieldLabel(fieldName)} es requerido`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldLabel(fieldName)} debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
      }
      if (field.errors['maxlength']) {
        return `${this.getFieldLabel(fieldName)} no puede tener más de ${field.errors['maxlength'].requiredLength} caracteres`;
      }
      if (field.errors['min']) {
        return `${this.getFieldLabel(fieldName)} debe ser mayor a 0`;
      }
    }
    return '';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      name: 'Nombre',
      description: 'Descripción',
      category: 'Categoría',
      unit: 'Unidad',
      price: 'Precio',
      isAvailable: 'Disponible'
    };
    return labels[fieldName] || fieldName;
  }

  canManageProducts(): boolean {
    return this.authService.hasRole('MANAGER') || this.authService.hasRole('ADMIN');
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(price);
  }

  generatePDF(): void {
    if (this.products.length === 0) {
      this.errorMessage = 'No hay productos para generar el PDF';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    try {
      const doc = new jsPDF();
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      let yPosition = 20;

      // Título principal
      doc.setFontSize(20);
      doc.setFont('helvetica', 'bold');
      doc.text('BOLETÍN DE PRECIOS', pageWidth / 2, yPosition, { align: 'center' });
      yPosition += 15;

      // Fecha de generación
      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      const currentDate = new Date().toLocaleDateString('es-CO', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      doc.text(`Generado el: ${currentDate}`, pageWidth / 2, yPosition, { align: 'center' });
      yPosition += 20;

      // Agrupar productos por categoría
      const groupedProducts: { [key: string]: ProductResponse[] } = {};
      this.products.forEach(product => {
        if (!groupedProducts[product.category]) {
          groupedProducts[product.category] = [];
        }
        groupedProducts[product.category].push(product);
      });

      // Generar contenido por categoría
      Object.keys(groupedProducts).forEach(category => {
        const categoryProducts = groupedProducts[category];
        
        // Verificar si hay espacio suficiente en la página
        if (yPosition > pageHeight - 60) {
          doc.addPage();
          yPosition = 20;
        }

        // Título de categoría
        doc.setFontSize(14);
        doc.setFont('helvetica', 'bold');
        doc.text(category.toUpperCase(), 20, yPosition);
        yPosition += 10;

        // Línea separadora
        doc.setLineWidth(0.5);
        doc.line(20, yPosition, pageWidth - 20, yPosition);
        yPosition += 8;

        // Encabezados de tabla
        doc.setFontSize(10);
        doc.setFont('helvetica', 'bold');
        doc.text('PRODUCTO', 20, yPosition);
        doc.text('UNIDAD', 100, yPosition);
        doc.text('PRECIO', 140, yPosition);
        doc.text('ESTADO', 170, yPosition);
        yPosition += 8;

        // Línea debajo de encabezados
        doc.setLineWidth(0.3);
        doc.line(20, yPosition, pageWidth - 20, yPosition);
        yPosition += 5;

        // Productos de la categoría
        categoryProducts.forEach(product => {
          // Verificar si hay espacio para el producto
          if (yPosition > pageHeight - 20) {
            doc.addPage();
            yPosition = 20;
          }

          doc.setFontSize(9);
          doc.setFont('helvetica', 'normal');
          
          // Nombre del producto
          const productName = product.name.length > 25 ? 
            product.name.substring(0, 22) + '...' : product.name;
          doc.text(productName, 20, yPosition);
          
          // Unidad
          doc.text(product.unit, 100, yPosition);
          
          // Precio
          doc.text(this.formatPrice(product.price), 140, yPosition);
          
          // Estado
          const status = product.isAvailable ? 'Disponible' : 'No disp.';
          doc.text(status, 170, yPosition);
          
          yPosition += 6;

          // Descripción si existe
          if (product.description && product.description.length > 0) {
            const description = product.description.length > 40 ? 
              product.description.substring(0, 37) + '...' : product.description;
            doc.setFontSize(8);
            doc.setFont('helvetica', 'italic');
            doc.text(`  ${description}`, 25, yPosition);
            yPosition += 5;
            doc.setFontSize(9);
            doc.setFont('helvetica', 'normal');
          }
        });

        yPosition += 15; // Espacio entre categorías
      });

      // Pie de página
      if (yPosition > pageHeight - 30) {
        doc.addPage();
        yPosition = pageHeight - 20;
      } else {
        yPosition = pageHeight - 20;
      }

      doc.setFontSize(8);
      doc.setFont('helvetica', 'italic');
      doc.text('Centro Comercial Plaza Central - Sistema de Gestión', pageWidth / 2, yPosition, { align: 'center' });

      // Generar y descargar el PDF
      const fileName = `boletin_precios_${new Date().toISOString().split('T')[0]}.pdf`;
      doc.save(fileName);

      this.successMessage = 'PDF generado exitosamente';
      this.isLoading = false;

    } catch (error) {
      console.error('Error generando PDF:', error);
      this.errorMessage = 'Error al generar el PDF';
      this.isLoading = false;
    }
  }
}
