package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for User responses
 */
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long plazaId;
    private String plazaName;
    private Set<RoleResponseDto> roles;
    private String fullName;
    private String externalId;
    
    // Constructors
    public UserResponseDto() {}
    
    public UserResponseDto(Long id, String username, String email, String firstName, String lastName,
                          String phoneNumber, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
                          Long plazaId, String plazaName, Set<RoleResponseDto> roles, String fullName, String externalId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.plazaId = plazaId;
        this.plazaName = plazaName;
        this.roles = roles;
        this.fullName = fullName;
        this.externalId = externalId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
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
    
    public Set<RoleResponseDto> getRoles() { return roles; }
    public void setRoles(Set<RoleResponseDto> roles) { this.roles = roles; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
