package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class ParqueaderoMockController {

    @GetMapping("/api/parqueadero")
    public Map<String, Object> getParqueadero() {
        Random rnd = new Random();
        int cuposTotales = 50;
        int ocupados = 20 + rnd.nextInt(20);
        double ocupacion = (ocupados * 100.0) / cuposTotales;
        List<Map<String, Object>> vehiculos = new ArrayList<>();
        
        for (int i = 0; i < ocupados; i++) {
            // Generar placa colombiana: 3 letras + 3 números (ej: ABC123)
            String letras = "";
            for (int j = 0; j < 3; j++) {
                letras += (char) ('A' + rnd.nextInt(26));
            }
            String numeros = String.format("%03d", rnd.nextInt(1000));
            String placa = letras + numeros;
            
            String entrada = String.format("%02d:%02d", 7 + rnd.nextInt(3), rnd.nextInt(60));
            String salida = String.format("%02d:%02d", 10 + rnd.nextInt(3), rnd.nextInt(60));
            int tarifa = 4000 + rnd.nextInt(3000);
            vehiculos.add(Map.of(
                "placa", placa,
                "entrada", entrada,
                "salida", salida,
                "tarifa", tarifa
            ));
        }
        return Map.of(
            "cuposTotales", cuposTotales,
            "ocupados", ocupados,
            "ocupacion", ocupacion,
            "vehiculos", vehiculos
        );
    }
}
