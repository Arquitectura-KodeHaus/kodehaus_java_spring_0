package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.service.ExternalSystemService;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Controller for managing modules from external system
 */
@RestController
@RequestMapping("/api/modulos")
@CrossOrigin(origins = "*")
public class ModuleController {

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
                return externalSystemService.getPlazaModules(null);
            }

            User currentUser = (User) authentication.getPrincipal();
            Plaza plaza = currentUser.getPlaza();

            // Get external_id if available, otherwise pass null (will fetch all modules)
            String externalId = (plaza != null && plaza.getExternalId() != null && !plaza.getExternalId().isBlank())
                    ? plaza.getExternalId()
                    : null;

            return externalSystemService.getPlazaModules(externalId);
        } catch (Exception e) {
            System.err.println("Error in ModuleController.getModules: " + e.getMessage());
            e.printStackTrace();
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
        return externalSystemService.getPlazaModules(plazaExternalId);
    }
}
