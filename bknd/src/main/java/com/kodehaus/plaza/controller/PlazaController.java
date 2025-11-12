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

    /**
     * Endpoint used by the external system to register a plaza.
     * Receives externalId and basic data, returns local id and uuid.
     */
    @PostMapping("/externo")
    // leave open to external system; authentication can be added later (API key, mutual TLS, etc.)
    public ResponseEntity<ExternalPlazaResponse> createPlazaFromExternal(@RequestBody ExternalPlazaRequest req) {
        if (req.getExternalId() == null || req.getExternalId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // If plaza already exists by externalId, return mapping
        plazaRepository.findByExternalId(req.getExternalId())
            .ifPresent(existing -> {
                // nothing - we'll return it below
            });

        Plaza plaza = plazaRepository.findByExternalId(req.getExternalId()).orElseGet(() -> {
            Plaza p = new Plaza();
            p.setName(req.getName() == null ? "(sin nombre)" : req.getName());
            p.setDescription(req.getDescription());
            p.setAddress(req.getAddress());
            p.setPhoneNumber(req.getPhoneNumber());
            p.setEmail(req.getEmail());
            p.setOpeningHours(req.getOpeningHours());
            p.setClosingHours(req.getClosingHours());
            p.setIsActive(true);
            p.setExternalId(req.getExternalId());
            return plazaRepository.save(p);
        });

        ExternalPlazaResponse resp = new ExternalPlazaResponse();
        resp.setId(plaza.getId());
        resp.setUuid(plaza.getUuid());
        resp.setExternalId(plaza.getExternalId());
        resp.setMessage("Plaza registrada/confirmada");
        return ResponseEntity.ok(resp);
    }
    
    @PutMapping("/{id}")
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
    public ResponseEntity<List<PlazaResponseDto>> getAllPlazas() {
        List<Plaza> plazas = plazaRepository.findByIsActiveTrue();
        
        List<PlazaResponseDto> response = plazas.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlazaResponseDto> getPlazaById(@PathVariable Long id) {
        return plazaRepository.findById(id)
            .filter(plaza -> plaza.getIsActive())
            .map(plaza -> ResponseEntity.ok(convertToResponseDto(plaza)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
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

    // DTOs for external endpoint
    public static class ExternalPlazaRequest {
        private String externalId;
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private String email;
        private String openingHours;
        private String closingHours;

        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
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

    public static class ExternalPlazaResponse {
        private Long id;
        private java.util.UUID uuid;
        private String externalId;
        private String message;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public java.util.UUID getUuid() { return uuid; }
        public void setUuid(java.util.UUID uuid) { this.uuid = uuid; }
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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
