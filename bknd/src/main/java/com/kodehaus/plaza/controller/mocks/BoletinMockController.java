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
        return List.of(
            Map.of("producto", "Tomate", "precio", 3200 + rnd.nextInt(500), "unidad", "kg", "fecha", fecha),
            Map.of("producto", "Papa criolla", "precio", 2500 + rnd.nextInt(400), "unidad", "kg", "fecha", fecha),
            Map.of("producto", "Carne de res", "precio", 14000 + rnd.nextInt(800), "unidad", "kg", "fecha", fecha),
            Map.of("producto", "Cebolla", "precio", 1800 + rnd.nextInt(300), "unidad", "kg", "fecha", fecha)
        );
    }
}
