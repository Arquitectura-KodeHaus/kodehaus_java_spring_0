package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for User creation/update requests
 */
public class UserRequestDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long plazaId;
    private Set<Long> roleIds;
    
    // Constructors
    public UserRequestDto() {}
    
    public UserRequestDto(String username, String email, String password, String firstName, String lastName,
                         String phoneNumber, Long plazaId, Set<Long> roleIds) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.plazaId = plazaId;
        this.roleIds = roleIds;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public Long getPlazaId() { return plazaId; }
    public void setPlazaId(Long plazaId) { this.plazaId = plazaId; }
    
    public Set<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds; }
}
