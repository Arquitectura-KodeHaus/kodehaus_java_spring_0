package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/boletin")
@CrossOrigin(origins = "*")
public class BoletinMockController {

    @GetMapping
    public List<Map<String, Object>> getBoletin() {
        Random rnd = new Random();
        String fecha = java.time.LocalDate.now().toString();
        // Variación porcentual (-8% a +8%) y categoría por producto
        return List.of(
            item("Tomate", "Verduras", 3200 + rnd.nextInt(500), "kg", fecha, rnd),
            item("Papa criolla", "Tubérculos", 2500 + rnd.nextInt(400), "kg", fecha, rnd),
            item("Cebolla cabezona", "Verduras", 1800 + rnd.nextInt(300), "kg", fecha, rnd),
            item("Aguacate Hass", "Frutas", 6000 + rnd.nextInt(1200), "kg", fecha, rnd),
            item("Banano", "Frutas", 1600 + rnd.nextInt(300), "kg", fecha, rnd),
            item("Carne de res", "Cárnicos", 14000 + rnd.nextInt(800), "kg", fecha, rnd),
            item("Pechuga de pollo", "Cárnicos", 9800 + rnd.nextInt(600), "kg", fecha, rnd),
            item("Queso campesino", "Lácteos", 11000 + rnd.nextInt(900), "kg", fecha, rnd)
        );
    }

    private Map<String, Object> item(String producto, String categoria, int precio, String unidad, String fecha, Random rnd) {
        int variacion = rnd.nextInt(17) - 8; // -8 a +8
        String tendencia = variacion > 0 ? "sube" : (variacion < 0 ? "baja" : "estable");
        return Map.of(
            "producto", producto,
            "categoria", categoria,
            "precio", precio,
            "unidad", unidad,
            "fecha", fecha,
            "variacion", variacion,
            "tendencia", tendencia
        );
    }
}
