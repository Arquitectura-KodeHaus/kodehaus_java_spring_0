package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Bulletin responses
 */
public class BulletinResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDate publicationDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long plazaId;
    private String plazaName;
    private Long createdById;
    private String createdByUsername;
    private String createdByFullName;
    
    // Constructors
    public BulletinResponseDto() {}
    
    public BulletinResponseDto(Long id, String title, String content, LocalDate publicationDate, Boolean isActive,
                              LocalDateTime createdAt, LocalDateTime updatedAt, Long plazaId, String plazaName,
                              Long createdById, String createdByUsername, String createdByFullName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publicationDate = publicationDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.plazaId = plazaId;
        this.plazaName = plazaName;
        this.createdById = createdById;
        this.createdByUsername = createdByUsername;
        this.createdByFullName = createdByFullName;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getPlazaId() { return plazaId; }
    public void setPlazaId(Long plazaId) { this.plazaId = plazaId; }
    
    public String getPlazaName() { return plazaName; }
    public void setPlazaName(String plazaName) { this.plazaName = plazaName; }
    
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    
    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
    
    public String getCreatedByFullName() { return createdByFullName; }
    public void setCreatedByFullName(String createdByFullName) { this.createdByFullName = createdByFullName; }
}
