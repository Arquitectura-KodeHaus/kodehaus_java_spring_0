package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.LoginRequestDto;
import com.kodehaus.plaza.dto.LoginResponseDto;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.security.JwtTokenProvider;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                         CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
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
        response.setExternalId(user.getPlaza().getExternalId());

        
        // ✅ VERIFICAR SI PLAZA EXISTE
        if (user.getPlaza() != null) {
            response.setPlazaId(user.getPlaza().getId());
            response.setPlazaName(user.getPlaza().getName());
        } else {
            response.setPlazaId(null);  // ✅ O un valor por defecto
            response.setPlazaName("Sin plaza asignada");
        }
        
        response.setRoles(user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet()));
        
        return ResponseEntity.ok(response);
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
