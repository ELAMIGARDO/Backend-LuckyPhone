package com.ventas.luckyphonedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricsCountsDTO {
    private long disponibles;      // Productos activos con stock > 0
    private long agotados;         // Productos con stock = 0
    private long anulados;         // Garantias / Proformas anuladas (soft delete)
    private long vigentes;         // Garantias con cobertura vigente
    private long caducados;        // Garantias con cobertura caducada
    private long totales;          // Total garantias emitidas no anuladas
    private long totalProductos;   // Total catalogo de productos
    private long itemsVendidos;    // Unidades celulares vendidas
    private long itemsDisponibles; // Unidades celulares disponibles
}