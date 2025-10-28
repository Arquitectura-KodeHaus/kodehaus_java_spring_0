package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.RoleResponseDto;
import com.kodehaus.plaza.dto.UserRequestDto;
import com.kodehaus.plaza.dto.UserResponseDto;
import com.kodehaus.plaza.entity.Role;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.repository.RoleRepository;
import com.kodehaus.plaza.repository.UserRepository;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.validation.Valid;
// Lombok annotations removed for compatibility
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Management Controller
 * 
 * Example JSON for creating user:
 * {
 *   "username": "employee1",
 *   "email": "employee@plaza.com",
 *   "password": "password123",
 *   "firstName": "Jane",
 *   "lastName": "Smith",
 *   "phoneNumber": "+1234567890",
 *   "plazaId": 1,
 *   "roleIds": [2, 3]
 * }
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlazaRepository plazaRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    
    public UserController(UserRepository userRepository, RoleRepository roleRepository,
                        PlazaRepository plazaRepository, PasswordEncoder passwordEncoder,
                        CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.plazaRepository = plazaRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<User> users = userRepository.findByPlazaIdAndIsActiveTrue(currentUser.getPlaza().getId());
        
        List<UserResponseDto> response = users.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return userRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(user -> ResponseEntity.ok(convertToResponseDto(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequest, 
                                                     Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if username already exists
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Verify plaza exists and user has access
        if (!plazaRepository.existsByIdAndIsActiveTrue(userRequest.getPlazaId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Create new user
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setPlaza(plazaRepository.findById(userRequest.getPlazaId()).orElse(null));
        
        // Set roles
        if (userRequest.getRoleIds() != null && !userRequest.getRoleIds().isEmpty()) {
            Set<Role> roles = roleRepository.findByIdInAndIsActiveTrue(userRequest.getRoleIds());
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedUser));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, 
                                                      @Valid @RequestBody UserRequestDto userRequest,
                                                      Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return userRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(user -> {
                // Check if username already exists (excluding current user)
                if (userRepository.existsByUsernameAndIdNot(userRequest.getUsername(), id)) {
                    return ResponseEntity.badRequest().<UserResponseDto>build();
                }
                
                // Check if email already exists (excluding current user)
                if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
                    return ResponseEntity.badRequest().<UserResponseDto>build();
                }
                
                // Update user fields
                user.setUsername(userRequest.getUsername());
                user.setEmail(userRequest.getEmail());
                if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
                }
                user.setFirstName(userRequest.getFirstName());
                user.setLastName(userRequest.getLastName());
                user.setPhoneNumber(userRequest.getPhoneNumber());
                
                // Update roles
                if (userRequest.getRoleIds() != null && !userRequest.getRoleIds().isEmpty()) {
                    Set<Role> roles = roleRepository.findByIdInAndIsActiveTrue(userRequest.getRoleIds());
                    user.setRoles(roles);
                }
                
                User savedUser = userRepository.save(user);
                return ResponseEntity.ok(convertToResponseDto(savedUser));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return userRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(user -> {
                user.setIsActive(false);
                userRepository.save(user);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
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
        
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                .<RoleResponseDto>map(role -> {
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
