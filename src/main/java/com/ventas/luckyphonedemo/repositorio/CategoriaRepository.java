package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.Categoria;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
