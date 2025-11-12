package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.StoreRequestDto;
import com.kodehaus.plaza.dto.StoreResponseDto;
import com.kodehaus.plaza.dto.StoreOwnerRequestDto;
import com.kodehaus.plaza.dto.UserResponseDto;
import com.kodehaus.plaza.entity.Store;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.entity.Role;
import com.kodehaus.plaza.repository.StoreRepository;
import com.kodehaus.plaza.repository.UserRepository;
import com.kodehaus.plaza.repository.RoleRepository;
import com.kodehaus.plaza.service.StoreManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Store Management Controller
 */
@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {
    
    private final StoreRepository storeRepository;
    // plazaRepository removed as it's not directly used in this controller
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreManagementService storeManagementService;
    
    public StoreController(StoreRepository storeRepository,
                          UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, StoreManagementService storeManagementService) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeManagementService = storeManagementService;
    }
    
    /**
     * Get all stores for the authenticated user's plaza
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('STORE_OWNER')")
    public ResponseEntity<List<StoreResponseDto>> getAllStores(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Store> stores = storeRepository.findByPlazaIdAndIsActiveTrue(currentUser.getPlaza().getId());
        
        List<StoreResponseDto> response = stores.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    
    /**
     * Get store by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDto> getStoreById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return storeRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(store -> ResponseEntity.ok(convertToResponseDto(store)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new store
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreRequestDto storeRequest,
                                                       Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if store name already exists in the plaza
        if (storeRepository.existsByNameAndPlazaId(storeRequest.getName(), currentUser.getPlaza().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Create store
        Store store = new Store();
        store.setName(storeRequest.getName());
        store.setDescription(storeRequest.getDescription());
        store.setOwnerName(storeRequest.getOwnerName());
        store.setPhoneNumber(storeRequest.getPhoneNumber());
        store.setEmail(storeRequest.getEmail());
        store.setPlaza(currentUser.getPlaza());
        store.setIsActive(true);
        
        Store savedStore = storeRepository.save(store);
        
        // Call external store management service (non-blocking)
        try {
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("name", savedStore.getName());
            storeData.put("description", savedStore.getDescription());
            storeData.put("ownerName", savedStore.getOwnerName());
            storeData.put("phoneNumber", savedStore.getPhoneNumber());
            storeData.put("email", savedStore.getEmail());
            if (currentUser.getPlaza().getExternalId() != null) {
                storeData.put("plazaExternalId", currentUser.getPlaza().getExternalId());
            }
            storeData.put("storeId", savedStore.getId());
            
            ResponseEntity<Map<String, Object>> externalResponse = storeManagementService.createStore(storeData);
            
            // If external service returns an external ID, update the store
            if (externalResponse != null && externalResponse.getStatusCode().is2xxSuccessful() && externalResponse.getBody() != null) {
                Map<String, Object> responseBody = externalResponse.getBody();
                if (responseBody.containsKey("externalId")) {
                    savedStore.setExternalId(responseBody.get("externalId").toString());
                    savedStore = storeRepository.save(savedStore);
                }
            } else {
                System.out.println("External store management service returned non-2xx response or no body");
            }
        } catch (Exception e) {
            System.err.println("Error calling store management service: " + e.getMessage());
            System.err.println("Store created locally, but external service call failed. This is not critical.");
            // Continue even if external service call fails - store is already saved locally
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedStore));
    }
    
    /**
     * Update store
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDto> updateStore(@PathVariable Long id,
                                                       @Valid @RequestBody StoreRequestDto storeRequest,
                                                       Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return storeRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(store -> {
                // Check if name already exists (excluding current store)
                if (storeRepository.existsByNameAndPlazaIdAndIdNot(storeRequest.getName(), 
                    currentUser.getPlaza().getId(), id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).<StoreResponseDto>build();
                }
                
                store.setName(storeRequest.getName());
                store.setDescription(storeRequest.getDescription());
                store.setOwnerName(storeRequest.getOwnerName());
                store.setPhoneNumber(storeRequest.getPhoneNumber());
                store.setEmail(storeRequest.getEmail());
                
                Store savedStore = storeRepository.save(store);
                return ResponseEntity.ok(convertToResponseDto(savedStore));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete store (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return storeRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(store -> {
                store.setIsActive(false);
                storeRepository.save(store);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create store owner profile
     */
    @PostMapping("/{storeId}/owner")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createStoreOwner(@PathVariable Long storeId,
                                                           @Valid @RequestBody StoreOwnerRequestDto ownerRequest,
                                                           Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Verify store exists and belongs to the plaza
        Store store = storeRepository.findByIdAndPlazaIdAndIsActiveTrue(storeId, currentUser.getPlaza().getId())
            .orElse(null);
        
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(ownerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(ownerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Create user (store owner)
        User owner = new User();
        owner.setUsername(ownerRequest.getUsername());
        owner.setEmail(ownerRequest.getEmail());
        owner.setPassword(passwordEncoder.encode(ownerRequest.getPassword()));
        owner.setFirstName(ownerRequest.getFirstName());
        owner.setLastName(ownerRequest.getLastName());
        owner.setPhoneNumber(ownerRequest.getPhoneNumber());
        owner.setPlaza(currentUser.getPlaza());
        owner.setStore(store);
        owner.setIsActive(true);
        
        // Assign STORE_OWNER role if it exists, otherwise assign EMPLOYEE_GENERAL
        Role storeOwnerRole = roleRepository.findByName("STORE_OWNER").orElse(null);
        if (storeOwnerRole == null) {
            storeOwnerRole = roleRepository.findByName("EMPLOYEE_GENERAL").orElse(null);
        }
        if (storeOwnerRole != null) {
            owner.setRoles(java.util.Set.of(storeOwnerRole));
        }
        
        User savedOwner = userRepository.save(owner);
        
        // Call external store management service to create owner profile (non-blocking)
        try {
            if (store.getExternalId() != null && currentUser.getPlaza().getExternalId() != null) {
                Map<String, Object> ownerData = new HashMap<>();
                ownerData.put("username", savedOwner.getUsername());
                ownerData.put("email", savedOwner.getEmail());
                ownerData.put("firstName", savedOwner.getFirstName());
                ownerData.put("lastName", savedOwner.getLastName());
                ownerData.put("phoneNumber", savedOwner.getPhoneNumber());
                ownerData.put("storeExternalId", store.getExternalId());
                ownerData.put("plazaExternalId", currentUser.getPlaza().getExternalId());
                
                ResponseEntity<Map<String, Object>> externalResponse = storeManagementService.createStoreOwnerProfile(store.getExternalId(), ownerData);
                
                if (externalResponse != null && externalResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Store owner profile created in external service successfully");
                } else {
                    System.out.println("External store management service returned non-2xx response for owner profile");
                }
            } else {
                System.out.println("Skipping external service call: store externalId or plaza externalId is null");
            }
        } catch (Exception e) {
            System.err.println("Error calling store management service for owner profile: " + e.getMessage());
            System.err.println("Store owner created locally, but external service call failed. This is not critical.");
            // Continue even if external service call fails - store owner is already saved locally
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToUserResponseDto(savedOwner));
    }
    
    private StoreResponseDto convertToResponseDto(Store store) {
        StoreResponseDto dto = new StoreResponseDto();
        dto.setId(store.getId());
        dto.setExternalId(store.getExternalId());
        dto.setName(store.getName());
        dto.setDescription(store.getDescription());
        dto.setOwnerName(store.getOwnerName());
        dto.setPhoneNumber(store.getPhoneNumber());
        dto.setEmail(store.getEmail());
        dto.setIsActive(store.getIsActive());
        dto.setPlazaId(store.getPlaza().getId());
        dto.setPlazaName(store.getPlaza().getName());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        return dto;
    }
    
    private UserResponseDto convertToUserResponseDto(User user) {
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
                .map(role -> {
                    com.kodehaus.plaza.dto.RoleResponseDto roleDto = new com.kodehaus.plaza.dto.RoleResponseDto();
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

