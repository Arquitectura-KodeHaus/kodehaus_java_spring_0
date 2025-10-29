package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.ProductRequestDto;
import com.kodehaus.plaza.dto.ProductResponseDto;
import com.kodehaus.plaza.entity.Product;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.repository.ProductRepository;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Management Controller
 * 
 * Example JSON for creating product:
 * {
 *   "name": "Tomatoes",
 *   "description": "Fresh red tomatoes",
 *   "category": "Vegetables",
 *   "unit": "kg",
 *   "price": 2000.00,
 *   "isAvailable": true
 * }
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductRepository productRepository;
    private final PlazaRepository plazaRepository;
    private final CustomUserDetailsService userDetailsService;
    
    public ProductController(ProductRepository productRepository, PlazaRepository plazaRepository,
                           CustomUserDetailsService userDetailsService) {
        this.productRepository = productRepository;
        this.plazaRepository = plazaRepository;
        this.userDetailsService = userDetailsService;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<Product> products = productRepository.findByPlazaIdOrderByCategoryAndName(currentUser.getPlaza().getId());
        
        List<ProductResponseDto> response = products.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<ProductResponseDto>> getAvailableProducts(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<Product> products = productRepository.findByPlazaIdAndIsActiveTrueAndIsAvailableTrue(currentUser.getPlaza().getId());
        
        List<ProductResponseDto> response = products.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<String>> getCategories(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<String> categories = productRepository.findDistinctCategoriesByPlazaId(currentUser.getPlaza().getId());
        
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return productRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(product -> ResponseEntity.ok(convertToResponseDto(product)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto productRequest, 
                                                          Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        // Verify plaza exists and user has access
        if (!plazaRepository.existsByIdAndIsActiveTrue(currentUser.getPlaza().getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Create new product
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setCategory(productRequest.getCategory());
        product.setUnit(productRequest.getUnit());
        product.setPrice(productRequest.getPrice());
        product.setIsAvailable(productRequest.getIsAvailable());
        product.setPlaza(currentUser.getPlaza());
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, 
                                                          @Valid @RequestBody ProductRequestDto productRequest,
                                                          Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return productRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(product -> {
                product.setName(productRequest.getName());
                product.setDescription(productRequest.getDescription());
                product.setCategory(productRequest.getCategory());
                product.setUnit(productRequest.getUnit());
                product.setPrice(productRequest.getPrice());
                product.setIsAvailable(productRequest.getIsAvailable());
                
                Product savedProduct = productRepository.save(product);
                return ResponseEntity.ok(convertToResponseDto(savedProduct));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return productRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(product -> {
                product.setIsActive(false);
                productRepository.save(product);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponseDto> updateProductPrice(@PathVariable Long id, 
                                                               @RequestBody ProductRequestDto priceRequest,
                                                               Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return productRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(product -> {
                product.setPrice(priceRequest.getPrice());
                Product savedProduct = productRepository.save(product);
                return ResponseEntity.ok(convertToResponseDto(savedProduct));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    private ProductResponseDto convertToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setUnit(product.getUnit());
        dto.setPrice(product.getPrice());
        dto.setIsActive(product.getIsActive());
        dto.setIsAvailable(product.getIsAvailable());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setPlazaId(product.getPlaza().getId());
        dto.setPlazaName(product.getPlaza().getName());
        
        return dto;
    }
}
