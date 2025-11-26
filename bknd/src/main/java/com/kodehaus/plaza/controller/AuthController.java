package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.LoginRequestDto;
import com.kodehaus.plaza.dto.LoginResponseDto;
import com.kodehaus.plaza.dto.UserRequestDto;
import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.Role;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.PermissionRepository;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.repository.RoleRepository;
import com.kodehaus.plaza.repository.UserRepository;
import com.kodehaus.plaza.security.JwtTokenProvider;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Controller
 * 
 * Example JSON for login:
 * {
 *   "username": "manager1",
 *   "password": "password123"
 * }
 * 
 * Response:
 * {
 *   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
 *   "tokenType": "Bearer",
 *   "id": 1,
 *   "username": "manager1",
 *   "email": "manager@plaza.com",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "fullName": "John Doe",
 *   "plazaId": 1,
 *   "plazaName": "Centro Comercial Plaza",
 *   "roles": ["MANAGER"]
 * }
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlazaRepository plazaRepository;
    private final PermissionRepository permissionRepository;
    
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                         CustomUserDetailsService userDetailsService, UserRepository userRepository,
                         RoleRepository roleRepository, PlazaRepository plazaRepository,
                         PermissionRepository permissionRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.plazaRepository = plazaRepository;
        this.permissionRepository = permissionRepository;
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = (User) authentication.getPrincipal();
        
        LoginResponseDto response = new LoginResponseDto();
        response.setAccessToken(jwt);
        response.setTokenType("Bearer");
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        
        // ✅ VERIFICAR SI PLAZA EXISTE
        if (user.getPlaza() != null) {
            response.setPlazaId(user.getPlaza().getId());
            response.setPlazaName(user.getPlaza().getName());
            response.setExternalId(user.getPlaza().getExternalId());
        } else {
            response.setPlazaId(null);  // ✅ O un valor por defecto
            response.setPlazaName("Sin plaza asignada");
            response.setExternalId(null);
        }
        
        response.setRoles(user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet()));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * External endpoint for registering a new user (called by external services)
     * 
     * Note: The plazaId field should contain the plaza's external_id (not the internal database ID)
     * Users registered through this endpoint will always be assigned the "gerente" role.
     * 
     * Example JSON for registration:
     * {
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "password": "password123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phoneNumber": "+1234567890",
     *   "plazaId": "12345"  // This is the external_id of the plaza
     * }
     * 
     * Response:
     * {
     *   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "tokenType": "Bearer",
     *   "id": 1,
     *   "username": "newuser",
     *   "email": "newuser@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "fullName": "John Doe",
     *   "plazaId": 1,  // Internal database ID
     *   "plazaName": "Centro Comercial Plaza",
     *   "roles": ["gerente"]
     * }
     */
    @PostMapping("/external-register")
    public ResponseEntity<?> externalRegister(@Valid @RequestBody UserRequestDto registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Email already exists");
        }
        
        // Find plaza by external_id (the plazaId received is actually the external_id)
        if (registerRequest.getPlazaId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Plaza external ID is required");
        }
        
        // Convert plazaId (Long) to String to use as externalId for lookup
        String plazaExternalId = String.valueOf(registerRequest.getPlazaId());
        Plaza plaza = plazaRepository.findByExternalId(plazaExternalId)
            .orElse(null);
        
        // Verify plaza exists and is active
        if (plaza == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Plaza not found with the provided external ID");
        }
        
        if (plaza.getIsActive() == null || !plaza.getIsActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Plaza is inactive");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Password stored as-is (not encoded)
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setPlaza(plaza); // Use the plaza found by externalId (with its internal ID)
        user.setIsActive(true);
        
        // Always assign "gerente" role (create it if it doesn't exist with all permissions)
        Role gerenteRole = roleRepository.findByName("gerente").orElse(null);
        if (gerenteRole == null) {
            // Create the "gerente" role if it doesn't exist with all permissions
            gerenteRole = new Role();
            gerenteRole.setName("gerente");
            gerenteRole.setDescription("Gerente de plaza con acceso completo");
            gerenteRole.setIsActive(true);
            // Assign all permissions to gerente role
            Set<com.kodehaus.plaza.entity.Permission> allPermissions = 
                new java.util.HashSet<>(permissionRepository.findByIsActiveTrue());
            gerenteRole.setPermissions(allPermissions);
            gerenteRole = roleRepository.save(gerenteRole);
        }
        user.setRoles(Set.of(gerenteRole));
        
        User savedUser = userRepository.save(user);
        
        // Automatically authenticate the newly registered user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                savedUser.getUsername(),
                registerRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        // Build response similar to login
        LoginResponseDto response = new LoginResponseDto();
        response.setAccessToken(jwt);
        response.setTokenType("Bearer");
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setFullName(savedUser.getFullName());
        response.setExternalId(savedUser.getPlaza() != null ? savedUser.getPlaza().getExternalId() : null);
        
        if (savedUser.getPlaza() != null) {
            response.setPlazaId(savedUser.getPlaza().getId());
            response.setPlazaName(savedUser.getPlaza().getName());
        } else {
            response.setPlazaId(null);
            response.setPlazaName("Sin plaza asignada");
        }
        
        response.setRoles(savedUser.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet()));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Successfully logged out");
    }
    
    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        String username = authentication.getName();
        User user = (User) userDetailsService.loadUserByUsername(username);
        
        LoginResponseDto response = new LoginResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        
        // ✅ VERIFICAR SI PLAZA EXISTE
        if (user.getPlaza() != null) {
            response.setPlazaId(user.getPlaza().getId());
            response.setPlazaName(user.getPlaza().getName());
        } else {
            response.setPlazaId(null);
            response.setPlazaName("Sin plaza asignada");
        }
        
        response.setRoles(user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet()));
        
        return ResponseEntity.ok(response);
    }
}
