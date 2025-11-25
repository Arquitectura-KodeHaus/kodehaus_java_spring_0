package com.kodehaus.plaza.service;

import com.kodehaus.plaza.entity.User;
import com.kodehaus.plaza.repository.UserRepository;
// Lombok annotations removed for compatibility
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom UserDetailsService implementation
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        log.warn("User loaded: {}", user);
        if (user.getPlaza() != null) {
            log.warn("Plaza ID: {}", user.getPlaza().getId());
            log.warn("Plaza externalId: {}", user.getPlaza().getExternalId());
        }   
        return user;
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findByIdWithPlaza(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return user;
    }
}
