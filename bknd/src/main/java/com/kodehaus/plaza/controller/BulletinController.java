package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.dto.BulletinRequestDto;
import com.kodehaus.plaza.dto.BulletinResponseDto;
import com.kodehaus.plaza.entity.Bulletin;
import com.kodehaus.plaza.entity.Plaza;
import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.BulletinRepository;
import com.kodehaus.plaza.repository.PlazaRepository;
import com.kodehaus.plaza.service.CustomUserDetailsService;
import com.kodehaus.plaza.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
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
    private final FileStorageService fileStorageService;
    
    public BulletinController(BulletinRepository bulletinRepository, PlazaRepository plazaRepository,
                            CustomUserDetailsService userDetailsService, FileStorageService fileStorageService) {
        this.bulletinRepository = bulletinRepository;
        this.plazaRepository = plazaRepository;
        this.userDetailsService = userDetailsService;
        this.fileStorageService = fileStorageService;
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
    @PreAuthorize("hasRole('MANAGER')")
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
        
        // Set file information if provided
        if (bulletinRequest.getFileName() != null) {
            bulletin.setFileName(bulletinRequest.getFileName());
            bulletin.setFilePath(bulletinRequest.getFilePath());
            bulletin.setFileType(bulletinRequest.getFileType());
            bulletin.setFileSize(bulletinRequest.getFileSize());
        }
        
        Bulletin savedBulletin = bulletinRepository.save(bulletin);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedBulletin));
    }
    
    @PostMapping(value = "/with-file", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BulletinResponseDto> createBulletinWithFile(
            @RequestParam("title") String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "publicationDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate,
            @RequestParam("plazaId") Long plazaId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        
        System.out.println("=== createBulletinWithFile called ===");
        System.out.println("Title: " + title);
        System.out.println("PlazaId: " + plazaId);
        System.out.println("File empty: " + (file == null || file.isEmpty()));
        
        String username = authentication.getName();
        System.out.println("Username: " + username);
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        System.out.println("User roles: " + currentUser.getAuthorities());
        
        // Verify plaza exists and user has access
        if (!plazaRepository.existsByIdAndIsActiveTrue(plazaId)) {
            return ResponseEntity.badRequest().build();
        }
        
        Bulletin bulletin = new Bulletin();
        bulletin.setTitle(title);
        bulletin.setContent(content != null ? content : ""); // Set empty content or the provided one
        bulletin.setPublicationDate(publicationDate != null ? publicationDate : LocalDate.now());
        
        // Get and set plaza
        Plaza plaza = plazaRepository.findById(plazaId).orElse(null);
        if (plaza == null) {
            return ResponseEntity.badRequest().build();
        }
        bulletin.setPlaza(plaza);
        bulletin.setCreatedBy(currentUser);
        
        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            try {
                // Save bulletin first to get ID
                Bulletin savedBulletin = bulletinRepository.save(bulletin);
                System.out.println("Bulletin saved with ID: " + savedBulletin.getId());
                
                // Store file
                FileStorageService.FileInfo fileInfo = fileStorageService.storeFile(file, savedBulletin.getId().toString());
                System.out.println("File stored: " + fileInfo.getOriginalName());
                
                // Update bulletin with file information
                savedBulletin.setFileName(fileInfo.getOriginalName());
                savedBulletin.setFilePath(fileInfo.getFilePath());
                savedBulletin.setFileType(fileInfo.getFileType());
                savedBulletin.setFileSize(fileInfo.getFileSize());
                
                Bulletin finalBulletin = bulletinRepository.save(savedBulletin);
                return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(finalBulletin));
                
            } catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            // No file provided, save bulletin without file
            Bulletin savedBulletin = bulletinRepository.save(bulletin);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedBulletin));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
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
    @PreAuthorize("hasRole('MANAGER')")
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
    
    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE_GENERAL', 'EMPLOYEE_SECURITY', 'EMPLOYEE_PARKING')")
    public ResponseEntity<?> downloadBulletinFile(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        return bulletinRepository.findByIdAndPlazaIdAndIsActiveTrue(id, currentUser.getPlaza().getId())
            .map(bulletin -> {
                if (bulletin.getFilePath() == null || bulletin.getFilePath().isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                try {
                    java.io.File file = new java.io.File(bulletin.getFilePath());
                    if (!file.exists()) {
                        return ResponseEntity.notFound().build();
                    }
                    
                    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
                    
                    return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + bulletin.getFileName() + "\"")
                        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, 
                                org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(resource);
                } catch (Exception e) {
                    System.err.println("Error downloading file: " + e.getMessage());
                    return ResponseEntity.internalServerError().build();
                }
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
        dto.setFileName(bulletin.getFileName());
        dto.setFilePath(bulletin.getFilePath());
        dto.setFileType(bulletin.getFileType());
        dto.setFileSize(bulletin.getFileSize());
        
        return dto;
    }
}
