package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

/**
 * DTO for Authentication requests
 */
public class LoginRequestDto {
    private String username;
    private String password;
    
    // Constructors
    public LoginRequestDto() {}
    
    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
