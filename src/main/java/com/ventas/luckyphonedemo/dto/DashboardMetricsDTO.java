package com.ventas.luckyphonedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    private int stockNetoReal;
    private int unidadesVendidas;
    private int unidadesDisponibles;
    private double ingresosEstimados;
    private int productosDisponibles;
    private int productosAgotados;
    private int totalProductos;
    private int garantiasVigentes;
    private int garantiasAnuladas;
    private double tasaRotacion;
}