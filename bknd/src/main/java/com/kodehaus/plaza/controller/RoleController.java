package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.PermissionResponseDto;
import com.kodehaus.plaza.dto.RoleResponseDto;
import com.kodehaus.plaza.entity.Permission;
import com.kodehaus.plaza.entity.Role;
import com.kodehaus.plaza.repository.PermissionRepository;
import com.kodehaus.plaza.repository.RoleRepository;
import jakarta.validation.Valid;
// Lombok annotations removed for compatibility
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Management Controller
 * 
 * Example JSON for creating role:
 * {
 *   "name": "EMPLOYEE_SECURITY",
 *   "description": "Security personnel role",
 *   "permissionIds": [1, 2, 3]
 * }
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<Role> roles = roleRepository.findByIsActiveTrue();
        
        List<RoleResponseDto> response = roles.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        return roleRepository.findById(id)
            .filter(role -> role.getIsActive())
            .map(role -> ResponseEntity.ok(convertToResponseDto(role)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto roleRequest) {
        // Check if role name already exists
        if (roleRepository.existsByName(roleRequest.getName())) {
            return ResponseEntity.badRequest().build();
        }
        
        Role role = new Role();
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        
        // Set permissions
        if (roleRequest.getPermissionIds() != null && !roleRequest.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByIdInAndIsActiveTrue(roleRequest.getPermissionIds());
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedRole));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id, 
                                                      @Valid @RequestBody RoleRequestDto roleRequest) {
        return roleRepository.findById(id)
            .filter(role -> role.getIsActive())
            .map(role -> {
                // Check if role name already exists (excluding current role)
                if (roleRepository.existsByNameAndIdNot(roleRequest.getName(), id)) {
                    return ResponseEntity.badRequest().<RoleResponseDto>build();
                }
                
                role.setName(roleRequest.getName());
                role.setDescription(roleRequest.getDescription());
                
                // Update permissions
                if (roleRequest.getPermissionIds() != null && !roleRequest.getPermissionIds().isEmpty()) {
                    Set<Permission> permissions = permissionRepository.findByIdInAndIsActiveTrue(roleRequest.getPermissionIds());
                    role.setPermissions(permissions);
                }
                
                Role savedRole = roleRepository.save(role);
                return ResponseEntity.ok(convertToResponseDto(savedRole));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return roleRepository.findById(id)
            .filter(role -> role.getIsActive())
            .map(role -> {
                role.setIsActive(false);
                roleRepository.save(role);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    private RoleResponseDto convertToResponseDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setIsActive(role.getIsActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        
        if (role.getPermissions() != null) {
            dto.setPermissions(role.getPermissions().stream()
                .map(permission -> {
                    PermissionResponseDto permissionDto = new PermissionResponseDto();
                    permissionDto.setId(permission.getId());
                    permissionDto.setName(permission.getName());
                    permissionDto.setDescription(permission.getDescription());
                    permissionDto.setResource(permission.getResource());
                    permissionDto.setAction(permission.getAction());
                    permissionDto.setFullPermission(permission.getFullPermission());
                    permissionDto.setIsActive(permission.getIsActive());
                    permissionDto.setCreatedAt(permission.getCreatedAt());
                    permissionDto.setUpdatedAt(permission.getUpdatedAt());
                    return permissionDto;
                })
                .collect(Collectors.toSet()));
        }
        
        return dto;
    }
    
    // Inner class for role requests
    public static class RoleRequestDto {
        private String name;
        private String description;
        private Set<Long> permissionIds;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Set<Long> getPermissionIds() { return permissionIds; }
        public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
    }
}
