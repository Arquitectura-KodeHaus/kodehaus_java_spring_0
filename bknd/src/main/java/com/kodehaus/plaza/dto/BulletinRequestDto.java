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
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    
    // Constructors
    public BulletinRequestDto() {}
    
    public BulletinRequestDto(String title, String content, LocalDate publicationDate, Long plazaId,
                            String fileName, String filePath, String fileType, Long fileSize) {
        this.title = title;
        this.content = content;
        this.publicationDate = publicationDate;
        this.plazaId = plazaId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
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
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
