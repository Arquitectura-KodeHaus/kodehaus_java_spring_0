package com.kodehaus.plaza.dto;

// Lombok annotations removed for compatibility

import java.time.LocalDate;

/**
 * DTO for Bulletin creation/update requests
 */
public class BulletinRequestDto {
    private String title;
    private String content;
    private LocalDate publicationDate;
    private Long plazaId;
    
    // Constructors
    public BulletinRequestDto() {}
    
    public BulletinRequestDto(String title, String content, LocalDate publicationDate, Long plazaId) {
        this.title = title;
        this.content = content;
        this.publicationDate = publicationDate;
        this.plazaId = plazaId;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    
    public Long getPlazaId() { return plazaId; }
    public void setPlazaId(Long plazaId) { this.plazaId = plazaId; }
}
