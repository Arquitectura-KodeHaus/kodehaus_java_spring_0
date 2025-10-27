package com.kodehaus.plaza.service;

import com.kodehaus.plaza.model.Plaza;
import com.kodehaus.plaza.repository.PlazaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlazaService {

    @Autowired
    private PlazaRepository repository;

    public List<Plaza> listar() { return repository.findAll(); }

    public Plaza obtener(Long id) { return repository.findById(id).orElse(null); }

    public Plaza guardar(Plaza plaza) { return repository.save(plaza); }

    public Plaza actualizar(Long id, Plaza nueva) {
        Plaza p = repository.findById(id).orElse(null);
        if (p == null) return null;
        p.setNombre(nueva.getNombre());
        p.setDireccion(nueva.getDireccion());
        p.setTelefono(nueva.getTelefono());
        p.setEmail(nueva.getEmail());
        p.setHorarioApertura(nueva.getHorarioApertura());
        p.setHorarioCierre(nueva.getHorarioCierre());
        p.setDescripcion(nueva.getDescripcion());
        return repository.save(p);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}
