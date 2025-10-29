package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.BulletinRequestDto;
import com.kodehaus.plaza.dto.BulletinResponseDto;
import com.kodehaus.plaza.entity.Bulletin;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.BulletinRepository;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.validation.Valid;
// Lombok annotations removed for compatibility
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bulletin Management Controller
 * 
 * Example JSON for creating bulletin:
 * {
 *   "title": "Daily Market Prices",
 *   "content": "Potatoes: $1000/kg, Tomatoes: $2000/kg, Onions: $1500/kg",
 *   "publicationDate": "2024-01-15",
 *   "plazaId": 1
 * }
 */
@RestController
@RequestMapping("/api/bulletins")
@CrossOrigin(origins = "*")
public class BulletinController {
    
    private final BulletinRepository bulletinRepository;
    private final PlazaRepository plazaRepository;
    private final CustomUserDetailsService userDetailsService;
    
    public BulletinController(BulletinRepository bulletinRepository, PlazaRepository plazaRepository,
                            CustomUserDetailsService userDetailsService) {
        this.bulletinRepository = bulletinRepository;
        this.plazaRepository = plazaRepository;
        this.userDetailsService = userDetailsService;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<BulletinResponseDto>> getAllBulletins(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<Bulletin> bulletins = bulletinRepository.findByPlazaIdAndIsActiveTrue(currentUser.getPlaza().getId());
        
        List<BulletinResponseDto> response = bulletins.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<BulletinResponseDto> getBulletinById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return bulletinRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(bulletin -> ResponseEntity.ok(convertToResponseDto(bulletin)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<BulletinResponseDto>> getTodaysBulletins(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<Bulletin> bulletins = bulletinRepository.findTodaysBulletinsByPlazaId(currentUser.getPlaza().getId());
        
        List<BulletinResponseDto> response = bulletins.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<List<BulletinResponseDto>> getBulletinsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        List<Bulletin> bulletins = bulletinRepository.findByPlazaIdAndPublicationDate(currentUser.getPlaza().getId(), date);
        
        List<BulletinResponseDto> response = bulletins.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<BulletinResponseDto> createBulletin(@Valid @RequestBody BulletinRequestDto bulletinRequest,
                                                              Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        // Verify plaza exists and user has access
        if (!plazaRepository.existsByIdAndIsActiveTrue(bulletinRequest.getPlazaId())) {
            return ResponseEntity.badRequest().build();
        }
        
        Bulletin bulletin = new Bulletin();
        bulletin.setTitle(bulletinRequest.getTitle());
        bulletin.setContent(bulletinRequest.getContent());
        bulletin.setPublicationDate(bulletinRequest.getPublicationDate() != null ? 
            bulletinRequest.getPublicationDate() : LocalDate.now());
        bulletin.setPlaza(plazaRepository.findById(bulletinRequest.getPlazaId()).orElse(null));
        bulletin.setCreatedBy(currentUser);
        
        Bulletin savedBulletin = bulletinRepository.save(bulletin);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedBulletin));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<BulletinResponseDto> updateBulletin(@PathVariable Long id,
                                                             @Valid @RequestBody BulletinRequestDto bulletinRequest,
                                                             Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return bulletinRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(bulletin -> {
                bulletin.setTitle(bulletinRequest.getTitle());
                bulletin.setContent(bulletinRequest.getContent());
                if (bulletinRequest.getPublicationDate() != null) {
                    bulletin.setPublicationDate(bulletinRequest.getPublicationDate());
                }
                
                Bulletin savedBulletin = bulletinRepository.save(bulletin);
                return ResponseEntity.ok(convertToResponseDto(savedBulletin));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<Void> deleteBulletin(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return bulletinRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(bulletin -> {
                bulletin.setIsActive(false);
                bulletinRepository.save(bulletin);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    private BulletinResponseDto convertToResponseDto(Bulletin bulletin) {
        BulletinResponseDto dto = new BulletinResponseDto();
        dto.setId(bulletin.getId());
        dto.setTitle(bulletin.getTitle());
        dto.setContent(bulletin.getContent());
        dto.setPublicationDate(bulletin.getPublicationDate());
        dto.setIsActive(bulletin.getIsActive());
        dto.setCreatedAt(bulletin.getCreatedAt());
        dto.setUpdatedAt(bulletin.getUpdatedAt());
        dto.setPlazaId(bulletin.getPlaza().getId());
        dto.setPlazaName(bulletin.getPlaza().getName());
        dto.setCreatedById(bulletin.getCreatedBy().getId());
        dto.setCreatedByUsername(bulletin.getCreatedBy().getUsername());
        dto.setCreatedByFullName(bulletin.getCreatedBy().getFullName());
        
        return dto;
    }
}
