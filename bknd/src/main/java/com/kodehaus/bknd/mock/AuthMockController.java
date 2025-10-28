package com.kodehaus.bknd.mock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthMockController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // Simula validación de usuario y contraseña
        String username = credentials.getOrDefault("username", "");
        String password = credentials.getOrDefault("password", "");
        if (username.equals("gerente") && password.equals("demo123")) {
            // Simula un token JWT (string random)
            String token = UUID.randomUUID().toString();
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                "id", 1,
                "username", "gerente",
                "roles", List.of("GERENTE"),
                "permissions", List.of("EDIT_PLAZA", "VIEW_REPORTS"),
                "plazaId", 1,
                "name", "Plaza Central"
            ));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
        }
    }
}
