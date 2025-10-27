package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class LocalesMockController {

    @GetMapping("/api/locales")
    public List<Map<String, Object>> getLocales() {
        return List.of(
            Map.of("nombre", "Frutas Don Pedro", "propietario", "Pedro Gómez", "ventas", 1200000, "empleados", 3),
            Map.of("nombre", "Carnes El Toro", "propietario", "Marta Ruiz", "ventas", 800000, "empleados", 2)
        );
    }
}
