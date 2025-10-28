package com.kodehaus.plaza.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// Lombok annotations removed for compatibility

import java.time.LocalDateTime;
import java.util.List;

/**
 * Plaza entity representing a shopping center or plaza
 */
@Entity
@Table(name = "plazas")
public class Plaza {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Plaza name is required")
    @Size(min = 2, max = 100, message = "Plaza name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(nullable = false)
    private String address;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(max = 100, message = "Opening hours must not exceed 100 characters")
    @Column(name = "opening_hours")
    private String openingHours;
    
    @Size(max = 100, message = "Closing hours must not exceed 100 characters")
    @Column(name = "closing_hours")
    private String closingHours;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationship with Users
    @OneToMany(mappedBy = "plaza", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
    
    // Relationship with Bulletins
    @OneToMany(mappedBy = "plaza", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bulletin> bulletins;
    
    // Constructors
    public Plaza() {}
    
    public Plaza(Long id, String name, String description, String address, String phoneNumber, String email,
                 String openingHours, String closingHours, Boolean isActive, LocalDateTime createdAt,
                 LocalDateTime updatedAt, List<User> users, List<Bulletin> bulletins) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.openingHours = openingHours;
        this.closingHours = closingHours;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.users = users;
        this.bulletins = bulletins;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    
    public String getClosingHours() { return closingHours; }
    public void setClosingHours(String closingHours) { this.closingHours = closingHours; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
    
    public List<Bulletin> getBulletins() { return bulletins; }
    public void setBulletins(List<Bulletin> bulletins) { this.bulletins = bulletins; }
}
