package com.kodehaus.plaza.controller;

import com.kodehaus.plaza.model.Plaza;
import com.kodehaus.plaza.service.PlazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plazas")
@CrossOrigin(origins = "*")
public class PlazaController {

    @Autowired
    private PlazaService service;

    @GetMapping
    public List<Plaza> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Plaza obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    public Plaza crear(@RequestBody Plaza plaza) { return service.guardar(plaza); }

    @PutMapping("/{id}")
    public Plaza actualizar(@PathVariable Long id, @RequestBody Plaza plaza) {
        return service.actualizar(id, plaza);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
