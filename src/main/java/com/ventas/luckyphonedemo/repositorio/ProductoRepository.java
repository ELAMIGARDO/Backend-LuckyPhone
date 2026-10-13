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

    /**
     * Productos Activos Disponibles (stock > 0 y activo = true):
     * Evaluando dinámicamente si es Celular (por items hijos Disponibles) o accesorio/cargador (por campo stock).
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND (" +
           "(p.categoria IS NOT NULL AND LOWER(p.categoria.nombre) = 'celulares' AND " +
           "(SELECT COUNT(ic) FROM ItemCelular ic WHERE ic.producto = p AND ic.estadoDispositivo = com.ventas.luckyphonedemo.model.EstadoDispositivo.Disponible) > 0) " +
           "OR ((p.categoria IS NULL OR LOWER(p.categoria.nombre) <> 'celulares') AND p.stock > 0))")
    List<Producto> findDisponiblesActivos();

    /**
     * Productos Agotados (stock = 0):
     */
    @Query("SELECT p FROM Producto p WHERE (" +
           "(p.categoria IS NOT NULL AND LOWER(p.categoria.nombre) = 'celulares' AND " +
           "(SELECT COUNT(ic) FROM ItemCelular ic WHERE ic.producto = p AND ic.estadoDispositivo = com.ventas.luckyphonedemo.model.EstadoDispositivo.Disponible) = 0) " +
           "OR ((p.categoria IS NULL OR LOWER(p.categoria.nombre) <> 'celulares') AND p.stock = 0))")
    List<Producto> findAgotados();

    /**
     * Conteo exacto de Productos Activos Disponibles
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true AND (" +
           "(p.categoria IS NOT NULL AND LOWER(p.categoria.nombre) = 'celulares' AND " +
           "(SELECT COUNT(ic) FROM ItemCelular ic WHERE ic.producto = p AND ic.estadoDispositivo = com.ventas.luckyphonedemo.model.EstadoDispositivo.Disponible) > 0) " +
           "OR ((p.categoria IS NULL OR LOWER(p.categoria.nombre) <> 'celulares') AND p.stock > 0))")
    long countDisponiblesActivos();

    /**
     * Conteo exacto de Productos Agotados
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE (" +
           "(p.categoria IS NOT NULL AND LOWER(p.categoria.nombre) = 'celulares' AND " +
           "(SELECT COUNT(ic) FROM ItemCelular ic WHERE ic.producto = p AND ic.estadoDispositivo = com.ventas.luckyphonedemo.model.EstadoDispositivo.Disponible) = 0) " +
           "OR ((p.categoria IS NULL OR LOWER(p.categoria.nombre) <> 'celulares') AND p.stock = 0))")
    long countAgotados();
}