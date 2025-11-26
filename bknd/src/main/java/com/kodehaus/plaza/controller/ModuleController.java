package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.service.ExternalSystemService;
import jakarta.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Controller for managing modules from external system
 */
@RestController
@RequestMapping({"/api/modules", "/api/modulos"})
@CrossOrigin(origins = "*")
public class ModuleController {
    private static final Logger log = LoggerFactory.getLogger(ModuleController.class);

    private final ExternalSystemService externalSystemService;

    public ModuleController(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }

    /**
     * Get modules for the authenticated user's plaza
     * Will attempt to get modules by plaza external_id if available,
     * otherwise will get all available modules from the external system
     */
    @GetMapping
    @PermitAll
    public ResponseEntity<List<Map<String, Object>>> getModules(Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                // No authentication, try to get all modules anyway
                log.info("No authentication, getting all modules");
                ResponseEntity<List<Map<String, Object>>> externalResponse = externalSystemService.getPlazaModules(null);
                // Extract body and create clean response to avoid 502 errors
                return ResponseEntity.ok(externalResponse.getBody());
            }

            User currentUser = (User) authentication.getPrincipal();
            log.info("Current user: {}", currentUser);
            Plaza plaza = currentUser.getPlaza();
            log.info("User plaza: {}", plaza);

            // Get external_id if available, otherwise pass null (will fetch all modules)
            String externalId = (plaza != null && plaza.getExternalId() != null && !plaza.getExternalId().isBlank())
                    ? plaza.getExternalId()
                    : null;
            log.info("Plaza externalId from DB: {}", externalId);
            
            ResponseEntity<List<Map<String, Object>>> externalResponse = externalSystemService.getPlazaModules(externalId);
            // Extract body and create clean response to avoid 502 errors
            List<Map<String, Object>> modules = externalResponse.getBody();
            if (modules == null) {
                log.warn("External service returned null body, returning empty list");
                return ResponseEntity.ok(List.of());
            }
            log.info("Returning {} modules to client", modules.size());
            return ResponseEntity.ok(modules);
        } catch (Exception e) {
            log.error("Error in ModuleController.getModules: {}", e.getMessage(), e);
            // Log error but return empty list to avoid breaking the frontend
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get modules for a specific plaza by external ID (for testing/admin purposes)
     */
    @GetMapping("/plaza/{plazaExternalId}")
    @PermitAll
    public ResponseEntity<List<Map<String, Object>>> getModulesByPlazaExternalId(@PathVariable String plazaExternalId) {
        try {
            ResponseEntity<List<Map<String, Object>>> externalResponse = externalSystemService.getPlazaModules(plazaExternalId);
            // Extract body and create clean response to avoid 502 errors
            List<Map<String, Object>> modules = externalResponse.getBody();
            if (modules == null) {
                log.warn("External service returned null body for plazaExternalId: {}, returning empty list", plazaExternalId);
                return ResponseEntity.ok(List.of());
            }
            log.info("Returning {} modules for plazaExternalId: {}", modules.size(), plazaExternalId);
            return ResponseEntity.ok(modules);
        } catch (Exception e) {
            log.error("Error in ModuleController.getModulesByPlazaExternalId: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }
}
