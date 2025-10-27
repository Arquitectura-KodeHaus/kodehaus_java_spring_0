package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ParqueaderoMockController {

    @GetMapping("/api/parqueadero")
    public List<Map<String, Object>> getParqueadero() {
        return List.of(
            Map.of("placa", "ABC123", "entrada", "08:00", "salida", "10:00", "tarifa", 5000),
            Map.of("placa", "XYZ987", "entrada", "09:30", "salida", "12:00", "tarifa", 7000)
        );
    }
}
