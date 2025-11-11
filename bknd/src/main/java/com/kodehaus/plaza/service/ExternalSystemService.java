package com.kodehaus.plaza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Service for communicating with the external System Owner (Software Owner)
 * Used to get modules and other information
 */
@Service
public class ExternalSystemService {
    
    @Value("${external.system-owner.url:http://localhost:8082}")
    private String systemOwnerUrl;
    
    @Value("${external.system-owner.api-key:}")
    private String systemOwnerApiKey;
    
    private final RestTemplate restTemplate;
    
    public ExternalSystemService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Get modules for a plaza from the external system owner
     * @param plazaExternalId External ID of the plaza
     * @return List of modules
     */
    public ResponseEntity<List<Map<String, Object>>> getPlazaModules(String plazaExternalId) {
        try {
            String url = systemOwnerUrl + "/api/plazas/" + plazaExternalId + "/modules";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            if (systemOwnerApiKey != null && !systemOwnerApiKey.isEmpty()) {
                headers.set("X-API-KEY", systemOwnerApiKey);
            }
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>> responseType = 
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {};
            
            return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
        } catch (RestClientException e) {
            System.err.println("Error calling external system owner service: " + e.getMessage());
            System.err.println("This is not critical - returning empty modules list. System will continue to work.");
            // Return empty list instead of throwing exception to avoid breaking the login flow
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            System.err.println("Unexpected error calling external system owner service: " + e.getMessage());
            // Return empty list to avoid breaking the flow
            return ResponseEntity.ok(List.of());
        }
    }
}

