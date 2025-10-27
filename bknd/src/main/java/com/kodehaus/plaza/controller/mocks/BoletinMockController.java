package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/boletin")
@CrossOrigin(origins = "*")
public class BoletinMockController {

    @GetMapping
    public List<Map<String, Object>> getBoletin() {
        return List.of(
            Map.of("producto", "Tomate", "precio", 3500, "unidad", "kg"),
            Map.of("producto", "Papa criolla", "precio", 2800, "unidad", "kg"),
            Map.of("producto", "Carne de res", "precio", 14500, "unidad", "kg")
        );
    }
}
