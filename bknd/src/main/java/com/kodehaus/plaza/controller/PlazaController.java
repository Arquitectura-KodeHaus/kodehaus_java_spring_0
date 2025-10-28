package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.repository.PlazaRepository;
// Lombok annotations removed for compatibility
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plaza Management Controller
 */
@RestController
@RequestMapping("/api/plazas")
@CrossOrigin(origins = "*")
public class PlazaController {
    
    private final PlazaRepository plazaRepository;
    
    public PlazaController(PlazaRepository plazaRepository) {
        this.plazaRepository = plazaRepository;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlazaResponseDto> createPlaza(@RequestBody PlazaCreateRequest req) {
        // Validaciones simples
        if (req.getName() == null || req.getName().isBlank() ||
            req.getAddress() == null || req.getAddress().isBlank() ||
            req.getPhoneNumber() == null || req.getPhoneNumber().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Plaza plaza = new Plaza();
        plaza.setName(req.getName());
        plaza.setDescription(req.getDescription());
        plaza.setAddress(req.getAddress());
        plaza.setPhoneNumber(req.getPhoneNumber());
        plaza.setEmail(req.getEmail());
        plaza.setOpeningHours(req.getOpeningHours());
        plaza.setClosingHours(req.getClosingHours());
        plaza.setIsActive(true);

        Plaza saved = plazaRepository.save(plaza);
        return ResponseEntity.ok(convertToResponseDto(saved));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlazaResponseDto> updatePlaza(@PathVariable Long id, @RequestBody PlazaUpdateRequest req) {
        return plazaRepository.findById(id)
            .filter(plaza -> plaza.getIsActive())
            .map(plaza -> {
                if (req.getName() != null) plaza.setName(req.getName());
                if (req.getDescription() != null) plaza.setDescription(req.getDescription());
                if (req.getAddress() != null) plaza.setAddress(req.getAddress());
                if (req.getPhoneNumber() != null) plaza.setPhoneNumber(req.getPhoneNumber());
                if (req.getEmail() != null) plaza.setEmail(req.getEmail());
                if (req.getOpeningHours() != null) plaza.setOpeningHours(req.getOpeningHours());
                if (req.getClosingHours() != null) plaza.setClosingHours(req.getClosingHours());
                Plaza saved = plazaRepository.save(plaza);
                return ResponseEntity.ok(convertToResponseDto(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DTO para actualización
    public static class PlazaUpdateRequest {
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private String email;
        private String openingHours; // formato HH:mm
        private String closingHours; // formato HH:mm

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOpeningHours() { return openingHours; }
        public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
        public String getClosingHours() { return closingHours; }
        public void setClosingHours(String closingHours) { this.closingHours = closingHours; }
    }
    
    // DTO para creación
    public static class PlazaCreateRequest {
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private String email;
        private String openingHours; // formato HH:mm
        private String closingHours; // formato HH:mm

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOpeningHours() { return openingHours; }
        public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
        public String getClosingHours() { return closingHours; }
        public void setClosingHours(String closingHours) { this.closingHours = closingHours; }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PlazaResponseDto>> getAllPlazas() {
        List<Plaza> plazas = plazaRepository.findByIsActiveTrue();
        
        List<PlazaResponseDto> response = plazas.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlazaResponseDto> getPlazaById(@PathVariable Long id) {
        return plazaRepository.findById(id)
            .filter(plaza -> plaza.getIsActive())
            .map(plaza -> ResponseEntity.ok(convertToResponseDto(plaza)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PlazaResponseDto>> searchPlazas(@RequestParam String name) {
        List<Plaza> plazas = plazaRepository.findByNameContainingIgnoreCase(name);
        
        List<PlazaResponseDto> response = plazas.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    private PlazaResponseDto convertToResponseDto(Plaza plaza) {
        PlazaResponseDto dto = new PlazaResponseDto();
        dto.setId(plaza.getId());
        dto.setName(plaza.getName());
        dto.setDescription(plaza.getDescription());
        dto.setAddress(plaza.getAddress());
        dto.setPhoneNumber(plaza.getPhoneNumber());
        dto.setEmail(plaza.getEmail());
        dto.setOpeningHours(plaza.getOpeningHours());
        dto.setClosingHours(plaza.getClosingHours());
        dto.setIsActive(plaza.getIsActive());
        dto.setCreatedAt(plaza.getCreatedAt());
        dto.setUpdatedAt(plaza.getUpdatedAt());
        
        return dto;
    }
    
    // Inner class for plaza responses
    public static class PlazaResponseDto {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private String email;
        private String openingHours;
        private String closingHours;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getOpeningHours() { return openingHours; }
        public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
        
        public String getClosingHours() { return closingHours; }
        public void setClosingHours(String closingHours) { this.closingHours = closingHours; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}