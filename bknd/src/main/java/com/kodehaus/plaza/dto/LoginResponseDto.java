package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.util.Set;

/**
 * DTO for Authentication responses
 */
public class LoginResponseDto {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Long plazaId;
    private String plazaName;
    private Set<String> roles;
    
    // Constructors
    public LoginResponseDto() {}
    
    public LoginResponseDto(String accessToken, String tokenType, Long id, String username, String email,
                          String firstName, String lastName, String fullName, Long plazaId, String plazaName,
                          Set<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.plazaId = plazaId;
        this.plazaName = plazaName;
        this.roles = roles;
    }
    
    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
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
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public Long getPlazaId() { return plazaId; }
    public void setPlazaId(Long plazaId) { this.plazaId = plazaId; }
    
    public String getPlazaName() { return plazaName; }
    public void setPlazaName(String plazaName) { this.plazaName = plazaName; }
    
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
