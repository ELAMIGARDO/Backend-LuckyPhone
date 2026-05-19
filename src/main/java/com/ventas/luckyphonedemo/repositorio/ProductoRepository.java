package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Page<Producto> findByActivoTrue(Pageable pageable);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}