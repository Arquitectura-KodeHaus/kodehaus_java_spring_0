package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@CrossOrigin(origins = "*")
public class PagosMockController {

    @GetMapping("/api/pagos")
    public List<Map<String, Object>> getPagos() {
        Random rnd = new Random();
        String[] tipos = {"Arriendo", "Comisión", "Suscripción"};
        String[] estados = {"Pagado", "Pendiente"};
        return List.of(
            Map.of(
                "concepto", "Arriendo mensual",
                "monto", 200000 + rnd.nextInt(60000),
                "fecha", "2025-10-01",
                "estado", estados[rnd.nextInt(estados.length)],
                "tipo", tipos[0],
                "referencia", "ARR-" + (1000 + rnd.nextInt(9000))
            ),
            Map.of(
                "concepto", "Servicio de limpieza",
                "monto", 40000 + rnd.nextInt(20000),
                "fecha", "2025-10-10",
                "estado", estados[rnd.nextInt(estados.length)],
                "tipo", tipos[1],
                "referencia", "COM-" + (1000 + rnd.nextInt(9000))
            ),
            Map.of(
                "concepto", "Suscripción mensual",
                "monto", 50000 + rnd.nextInt(10000),
                "fecha", "2025-10-15",
                "estado", estados[rnd.nextInt(estados.length)],
                "tipo", tipos[2],
                "referencia", "SUS-" + (1000 + rnd.nextInt(9000))
            )
        );
    }
}
