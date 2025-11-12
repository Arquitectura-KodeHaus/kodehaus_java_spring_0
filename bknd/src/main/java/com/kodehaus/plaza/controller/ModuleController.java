package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.service.ExternalSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Controller for managing modules from external system
 */
@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {
    
    private final ExternalSystemService externalSystemService;
    
    public ModuleController(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }
    
    /**
     * Get modules for the authenticated user's plaza
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getModules(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Plaza plaza = currentUser.getPlaza();
        
        if (plaza == null || plaza.getExternalId() == null) {
            return ResponseEntity.ok(List.of());
        }
        
        ResponseEntity<List<Map<String, Object>>> response = externalSystemService.getPlazaModules(plaza.getExternalId());
        return response;
    }
    
    /**
     * Get modules for a specific plaza by external ID (for testing/admin purposes)
     */
    @GetMapping("/plaza/{plazaExternalId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getModulesByPlazaExternalId(@PathVariable String plazaExternalId) {
        ResponseEntity<List<Map<String, Object>>> response = externalSystemService.getPlazaModules(plazaExternalId);
        return response;
    }
}

