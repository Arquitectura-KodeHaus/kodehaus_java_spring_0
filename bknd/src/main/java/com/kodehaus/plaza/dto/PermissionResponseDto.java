package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.time.LocalDateTime;

/**
 * DTO for Permission responses
 */
public class PermissionResponseDto {
    private Long id;
    private String name;
    private String description;
    private String resource;
    private String action;
    private String fullPermission;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public PermissionResponseDto() {}
    
    public PermissionResponseDto(Long id, String name, String description, String resource, String action,
                               String fullPermission, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
        this.fullPermission = fullPermission;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getFullPermission() { return fullPermission; }
    public void setFullPermission(String fullPermission) { this.fullPermission = fullPermission; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
