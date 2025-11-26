package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.PermissionResponseDto;
import com.kodehaus.plaza.entity.Permission;
import com.kodehaus.plaza.repository.PermissionRepository;
// Lombok annotations removed for compatibility
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Permission Management Controller
 */
@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {
    
    private final PermissionRepository permissionRepository;
    
    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('gerente')")
    public ResponseEntity<List<PermissionResponseDto>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findByIsActiveTrue();
        
        List<PermissionResponseDto> response = permissions.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('gerente')")
    public ResponseEntity<PermissionResponseDto> getPermissionById(@PathVariable Long id) {
        return permissionRepository.findById(id)
            .filter(permission -> permission.getIsActive())
            .map(permission -> ResponseEntity.ok(convertToResponseDto(permission)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/resource/{resource}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('gerente')")
    public ResponseEntity<List<PermissionResponseDto>> getPermissionsByResource(@PathVariable String resource) {
        List<Permission> permissions = permissionRepository.findByResource(resource);
        
        List<PermissionResponseDto> response = permissions.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    private PermissionResponseDto convertToResponseDto(Permission permission) {
        PermissionResponseDto dto = new PermissionResponseDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setFullPermission(permission.getFullPermission());
        dto.setIsActive(permission.getIsActive());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        
        return dto;
    }
}
