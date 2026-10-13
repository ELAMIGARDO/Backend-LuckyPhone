package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.ItemCelular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCelularRepository extends JpaRepository<ItemCelular, Long> {

    Optional<ItemCelular> findByImei1(String imei1);

    boolean existsByImei1(String imei1);

    boolean existsByImei1AndEstadoDispositivo(String imei1, EstadoDispositivo estado);

    List<ItemCelular> findByProductoIdAndEstadoDispositivo(Long productoId, EstadoDispositivo estado);

    long countByProductoIdAndEstadoDispositivo(Long productoId, EstadoDispositivo estado);

    List<ItemCelular> findByEstadoDispositivo(EstadoDispositivo estado);

    long countByEstadoDispositivo(EstadoDispositivo estado);
}