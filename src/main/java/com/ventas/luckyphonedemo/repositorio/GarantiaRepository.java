package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.Garantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarantiaRepository extends JpaRepository<Garantia, String> {

    // Obtener garantias activas (No anuladas)
    List<Garantia> findByEliminadoAtIsNullOrderByFechaEmisionDesc();

    // Obtener garantias anuladas (Papelera de reciclaje)
    List<Garantia> findByEliminadoAtIsNotNullOrderByFechaEmisionDesc();
}