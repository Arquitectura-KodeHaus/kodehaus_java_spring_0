package com.kodehaus.plaza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * Service for communicating with the external System Owner (Software Owner)
 * Used to get modules and other information
 */
@Service
public class ExternalSystemService {
    private static final Logger log = LoggerFactory.getLogger(ExternalSystemService.class);
    
    @Value("${external.system-owner.url:http://localhost:8082}")
    private String systemOwnerUrl;
    
    @Value("${external.system-owner.api-key:}")
    private String systemOwnerApiKey;
    
    private final RestTemplate restTemplate;
    
    public ExternalSystemService() {
        this.restTemplate = createRestTemplate();
    }
    
    /**
     * Create RestTemplate with proper SSL/TLS configuration for HTTPS connections
     */
    private RestTemplate createRestTemplate() {
        try {
            // Create a trust manager that accepts all certificates
            // WARNING: This is for development/testing. In production, use proper certificate validation
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            
            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Create an SSL socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            
            // Create request factory with SSL support
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
                    super.prepareConnection(connection, httpMethod);
                    if (connection instanceof HttpsURLConnection) {
                        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                        httpsConnection.setSSLSocketFactory(sslSocketFactory);
                        httpsConnection.setHostnameVerifier((hostname, session) -> true);
                    }
                }
            };
            
            // Set timeouts to prevent hanging connections
            factory.setConnectTimeout(15000); // 15 seconds
            factory.setReadTimeout(30000); // 30 seconds
            
            RestTemplate template = new RestTemplate(factory);
            log.info("RestTemplate configured with SSL support and timeouts");
            return template;
        } catch (Exception e) {
            log.error("Error creating RestTemplate with SSL support, using default with timeouts: {}", e.getMessage(), e);
            // Fallback to default RestTemplate with timeouts
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(30000);
            return new RestTemplate(factory);
        }
    }
    
    /**
     * Get modules for a plaza from the external system owner
     * @param plazaExternalId External ID of the plaza (can be null if getting all modules)
     * @return List of modules
     */
    public ResponseEntity<List<Map<String, Object>>> getPlazaModules(String plazaExternalId) {
        String url = null;
        try {
            // Try to get modules by plaza first, if that fails or plazaExternalId is null, get all modules
            log.info("External ID: {}", plazaExternalId);
            if (plazaExternalId != null && !plazaExternalId.isBlank()) {
                url = systemOwnerUrl + "/api/modulos/plaza/" + plazaExternalId;
            } else {
                // Fallback to getting all modules (simplified approach for stocks-backend)
                url = systemOwnerUrl + "/api/modulos";
            }
            
            log.info("URL Modulos: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            if (systemOwnerApiKey != null && !systemOwnerApiKey.isEmpty()) {
                headers.set("X-API-KEY", systemOwnerApiKey);
            }
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>> responseType = 
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {};
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, request, responseType);
            
            log.info("✅ Successfully fetched modules from: {}", url);
            return response;
        } catch (RestClientException e) {
            log.error("RestClientException calling external system owner service: {}", e.getMessage());
            log.error("URL attempted: {}", url != null ? url : systemOwnerUrl);
            log.error("Exception details: ", e);
            // Return empty list instead of throwing exception to avoid breaking the login flow
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Unexpected error calling external system owner service: {}", e.getMessage(), e);
            log.error("URL attempted: {}", url != null ? url : systemOwnerUrl);
            // Return empty list to avoid breaking the flow
            return ResponseEntity.ok(List.of());
        }
    }
}

