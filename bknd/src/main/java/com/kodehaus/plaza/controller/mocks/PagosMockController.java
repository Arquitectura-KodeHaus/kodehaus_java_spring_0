package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class PagosMockController {

    @GetMapping("/api/pagos")
    public List<Map<String, Object>> getPagos() {
        return List.of(
            Map.of("concepto", "Arriendo mensual", "monto", 250000, "fecha", "2025-10-01", "estado", "Pagado"),
            Map.of("concepto", "Servicio de limpieza", "monto", 50000, "fecha", "2025-10-10", "estado", "Pendiente")
        );
    }
}
