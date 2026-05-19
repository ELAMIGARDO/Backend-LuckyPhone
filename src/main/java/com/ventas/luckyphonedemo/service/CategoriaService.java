package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.repositorio.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class CategoriaService {
    @Autowired CategoriaRepository repository;

    public List<Categoria> obtenerTodas(){
        return repository.findAll();
    }

    public Categoria obtenerPorId(Long id){
        return repository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Categoria no encontrada"));
    }

    public Categoria crear(Categoria categoria){
        return repository.save(categoria);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, categoría no existe");
        }
        repository.deleteById(id);
    }
}
