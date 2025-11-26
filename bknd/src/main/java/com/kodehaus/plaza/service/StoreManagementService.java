package com.kodehaus.plaza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Service for communicating with the external Store Management System
 */
@Service
public class StoreManagementService {
    
    @Value("${external.store-management.url:http://localhost:8090}")
    private String storeManagementUrl;
    
    @Value("${external.store-management.api-key:}")
    private String storeManagementApiKey;
    
    private final RestTemplate restTemplate;
    
    public StoreManagementService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Create a store in the external store management system
     * @param storeData Store data to create
     * @return Response from external system
     */
    public ResponseEntity<Map<String, Object>> createStore(Map<String, Object> storeData) {
        try {
            String url = storeManagementUrl + "/api/Locales";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            if (storeManagementApiKey != null && !storeManagementApiKey.isEmpty()) {
                headers.set("X-API-KEY", storeManagementApiKey);
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(storeData, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, request, 
                (Class<Map<String, Object>>)(Class<?>)Map.class);
        } catch (RestClientException e) {
            System.err.println("Error calling store management service: " + e.getMessage());
            // Don't throw exception - return error response instead to avoid breaking the flow
            // The store is still created locally even if external service fails
            return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                .body(java.util.Map.of("error", "External service unavailable", "message", e.getMessage()));
        }
    }
    
    /**
     * Create a store owner profile in the external store management system
     * @param storeExternalId Store external ID
     * @param ownerData Owner profile data
     * @return Response from external system
     */
    public ResponseEntity<Map<String, Object>> createStoreOwnerProfile(String storeExternalId, Map<String, Object> ownerData) {
        try {
            String url = storeManagementUrl + "/api/stores/" + storeExternalId + "/owner";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            if (storeManagementApiKey != null && !storeManagementApiKey.isEmpty()) {
                headers.set("X-API-KEY", storeManagementApiKey);
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(ownerData, headers);
            
            return restTemplate.exchange(url, HttpMethod.POST, request, 
                (Class<Map<String, Object>>)(Class<?>)Map.class);
        } catch (RestClientException e) {
            System.err.println("Error calling store management service for owner profile: " + e.getMessage());
            // Don't throw exception - return error response instead to avoid breaking the flow
            // The store owner is still created locally even if external service fails
            return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                .body(java.util.Map.of("error", "External service unavailable", "message", e.getMessage()));
        }
    }
}

