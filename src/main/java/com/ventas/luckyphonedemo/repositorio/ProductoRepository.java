package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Page<Producto> findByActivoTrue(Pageable pageable);
    
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Busca la entidad Padre exacta combinando Modelo + Color + Almacenamiento + Estado (Condicion)
     */
    Optional<Producto> findByModeloIgnoreCaseAndColorIgnoreCaseAndAlmacenamientoIgnoreCaseAndEstadoIgnoreCase(
            String modelo, String color, String almacenamiento, String estado
    );

    /**
     * Extrae de forma unica todos los strings de color no nulos ni vacios registrados en productos
     */
    @Query("SELECT DISTINCT TRIM(p.color) FROM Producto p WHERE p.color IS NOT NULL AND TRIM(p.color) <> '' ORDER BY TRIM(p.color) ASC")
    List<String> findDistinctColores();
}