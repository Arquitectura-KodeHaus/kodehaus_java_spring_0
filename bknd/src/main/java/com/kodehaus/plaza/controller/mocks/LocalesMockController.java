package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@CrossOrigin(origins = "*")
public class LocalesMockController {

    @GetMapping("/api/locales")
    public List<Map<String, Object>> getLocales() {
        Random rnd = new Random();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> locales = new ArrayList<>();

        locales.add(Map.of(
            "nombre", "Frutas Don Pedro",
            "propietario", "Pedro Gómez",
            "ventasDia", 100000 + rnd.nextInt(50000),
            "ventasMes", 1200000 + rnd.nextInt(300000),
            "inventario", List.of(
                Map.of("producto", "Tomate", "stock", 20 + rnd.nextInt(10)),
                Map.of("producto", "Banano", "stock", 15 + rnd.nextInt(10))
            ),
            "empleados", List.of(
                Map.of("nombre", "Ana Torres", "rol", "Vendedora"),
                Map.of("nombre", "Luis Pérez", "rol", "Cajero"),
                Map.of("nombre", "Sofía Díaz", "rol", "Auxiliar")
            ),
            "ultimaVenta", LocalDate.now().minusDays(rnd.nextInt(2)).format(fmt)
        ));

        locales.add(Map.of(
            "nombre", "Carnes El Toro",
            "propietario", "Marta Ruiz",
            "ventasDia", 80000 + rnd.nextInt(40000),
            "ventasMes", 800000 + rnd.nextInt(200000),
            "inventario", List.of(
                Map.of("producto", "Carne de res", "stock", 10 + rnd.nextInt(5)),
                Map.of("producto", "Pollo", "stock", 8 + rnd.nextInt(5))
            ),
            "empleados", List.of(
                Map.of("nombre", "Carlos Gómez", "rol", "Carnicero"),
                Map.of("nombre", "María López", "rol", "Vendedora")
            ),
            "ultimaVenta", LocalDate.now().minusDays(rnd.nextInt(2)).format(fmt)
        ));

        return locales;
    }
}
