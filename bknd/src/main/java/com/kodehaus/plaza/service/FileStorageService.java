package com.kodehaus.plaza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for handling file uploads and management
 */
@Service
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.max-size:10485760}") // 10MB default
    private long maxFileSize;
    
    private static final String[] ALLOWED_EXTENSIONS = {
        "pdf", "doc", "docx", "xls", "xlsx", "txt", "jpg", "jpeg", "png", "gif"
    };
    
    /**
     * Store uploaded file and return file information
     */
    public FileInfo storeFile(MultipartFile file, String bulletinId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }
        
        String fileExtension = getFileExtension(originalFileName);
        if (!isAllowedExtension(fileExtension)) {
            throw new IllegalArgumentException("File type not allowed");
        }
        
        // Create directory structure: uploads/bulletins/{bulletinId}/
        Path bulletinDir = Paths.get(uploadDir, "bulletins", bulletinId);
        Files.createDirectories(bulletinDir);
        
        // Generate unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFileName = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + fileExtension;
        
        Path filePath = bulletinDir.resolve(uniqueFileName);
        
        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return new FileInfo(
            originalFileName,
            filePath.toString(),
            fileExtension,
            file.getSize(),
            file.getContentType()
        );
    }
    
    /**
     * Delete file from storage
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Check if file extension is allowed
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * File information container
     */
    public static class FileInfo {
        private final String originalName;
        private final String filePath;
        private final String fileType;
        private final long fileSize;
        private final String contentType;
        
        public FileInfo(String originalName, String filePath, String fileType, long fileSize, String contentType) {
            this.originalName = originalName;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }
        
        public String getOriginalName() { return originalName; }
        public String getFilePath() { return filePath; }
        public String getFileType() { return fileType; }
        public long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
    }
}
