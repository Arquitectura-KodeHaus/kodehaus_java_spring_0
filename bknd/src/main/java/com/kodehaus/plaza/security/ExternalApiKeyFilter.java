package com.kodehaus.plaza.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ExternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${external.api.key}")
    private String externalApiKey;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PROTECTED_PATHS = Arrays.asList(
            "/api/plazas/externo",
            "/api/users/externo"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // DESACTIVADO PARA DESARROLLO
        /*
        String path = request.getRequestURI();

        boolean shouldProtect = PROTECTED_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));

        if (shouldProtect) {
            String key = request.getHeader("X-API-KEY");
            if (key == null || key.isEmpty() || !key.equals(externalApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                return;
            }
        }
        */

        filterChain.doFilter(request, response);
    }
}
