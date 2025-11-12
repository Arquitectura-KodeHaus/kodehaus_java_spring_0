package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.RoleResponseDto;
import com.kodehaus.plaza.dto.UserRequestDto;
import com.kodehaus.plaza.dto.UserResponseDto;
import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.Role;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.repository.RoleRepository;
import com.kodehaus.plaza.repository.UserRepository;
import jakarta.validation.Valid;
// Lombok annotations removed for compatibility
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manager Registration Controller (for inter-service communication)
 * This endpoint is used by the System Service to create manager accounts
 * 
 * Example JSON for manager registration:
 * {
 *   "username": "manager1",
 *   "email": "manager@plaza.com",
 *   "password": "password123",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "phoneNumber": "+1234567890",
 *   "plazaId": 1,
 *   "roleIds": [1]
 * }
 */
@RestController
@RequestMapping("/api/managers")
@CrossOrigin(origins = "*")
public class ManagerController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlazaRepository plazaRepository;
    private final PasswordEncoder passwordEncoder;
    
    public ManagerController(UserRepository userRepository, RoleRepository roleRepository,
                           PlazaRepository plazaRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.plazaRepository = plazaRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerManager(@Valid @RequestBody UserRequestDto managerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(managerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(managerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }
        
        // Check if externalId already exists (if provided)
        if (managerRequest.getExternalId() != null && !managerRequest.getExternalId().isBlank()) {
            if (userRepository.findByExternalId(managerRequest.getExternalId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("External ID already exists");
            }
        }
        
        // Verify plaza exists
        Plaza plaza = plazaRepository.findById(managerRequest.getPlazaId()).orElse(null);
        if (plaza == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Plaza not found with id: " + managerRequest.getPlazaId());
        }
        
        // Verify plaza is active
        if (!plaza.getIsActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Plaza is not active");
        }
        
        // Create new manager user
        User manager = new User();
        manager.setUsername(managerRequest.getUsername());
        manager.setEmail(managerRequest.getEmail());
        manager.setPassword(passwordEncoder.encode(managerRequest.getPassword()));
        manager.setFirstName(managerRequest.getFirstName());
        manager.setLastName(managerRequest.getLastName());
        manager.setPhoneNumber(managerRequest.getPhoneNumber());
        manager.setPlaza(plaza);
        
        // Set externalId if provided
        if (managerRequest.getExternalId() != null && !managerRequest.getExternalId().isBlank()) {
            manager.setExternalId(managerRequest.getExternalId());
        }
        
        // Set roles (should include MANAGER role)
        if (managerRequest.getRoleIds() != null && !managerRequest.getRoleIds().isEmpty()) {
            Set<Role> roles = roleRepository.findByIdInAndIsActiveTrue(managerRequest.getRoleIds());
            manager.setRoles(roles);
        } else {
            // Assign MANAGER role by default
            roleRepository.findByName("MANAGER").ifPresent(managerRole -> {
                manager.setRoles(Set.of(managerRole));
            });
        }
        
        User savedManager = userRepository.save(manager);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedManager));
    }
    
    @GetMapping("/{plazaId}")
    public ResponseEntity<UserResponseDto> getManagerByPlazaId(@PathVariable Long plazaId) {
        return userRepository.findByPlazaIdAndRoleName(plazaId, "MANAGER")
            .stream()
            .findFirst()
            .map(manager -> ResponseEntity.ok(convertToResponseDto(manager)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{plazaId}/exists")
    public ResponseEntity<Boolean> checkManagerExists(@PathVariable Long plazaId) {
        boolean exists = !userRepository.findByPlazaIdAndRoleName(plazaId, "MANAGER").isEmpty();
        return ResponseEntity.ok(exists);
    }
    
    private UserResponseDto convertToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setPlazaId(user.getPlaza().getId());
        dto.setPlazaName(user.getPlaza().getName());
        dto.setFullName(user.getFullName());
        dto.setExternalId(user.getExternalId());
        
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                .map(role -> {
                    RoleResponseDto roleDto = new RoleResponseDto();
                    roleDto.setId(role.getId());
                    roleDto.setName(role.getName());
                    roleDto.setDescription(role.getDescription());
                    roleDto.setIsActive(role.getIsActive());
                    roleDto.setCreatedAt(role.getCreatedAt());
                    roleDto.setUpdatedAt(role.getUpdatedAt());
                    return roleDto;
                })
                .collect(Collectors.toSet()));
        }
        
        return dto;
    }
}
