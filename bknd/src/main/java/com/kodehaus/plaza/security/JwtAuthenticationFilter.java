package com.kodehaus.plaza.security;

import com.kodehaus.plaza.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // ✅ Rutas que NO necesitan autenticación JWT (con wildcards)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/actuator/**",
        "/api/auth/**",
        "/api/managers/register",
        "/h2-console/**",
        "/error",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        
        try {
            // ✅ Si es una ruta pública, NO validar JWT
            if (isPublicPath(path)) {
                log.debug("🔓 Skipping JWT filter for public path: {}", path);
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ JWT validated for user: {} on path: {}", username, path);
            } else {
                log.debug("⚠️ No valid JWT found in request to: {}", path);
            }
        } catch (Exception ex) {
            log.error("❌ Could not set user authentication in security context for path: {}", path, ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    // ✅ Verificar si la ruta es pública (usando AntPathMatcher para wildcards)
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
