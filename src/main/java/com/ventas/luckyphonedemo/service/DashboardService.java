package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.DashboardMetricsDTO;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.Garantia;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.GarantiaRepository;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final ItemCelularRepository itemCelularRepository;
    private final GarantiaRepository garantiaRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsDTO obtenerMetricas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Producto> productos = productoRepository.findAll();
        List<ItemCelular> items = itemCelularRepository.findAll();
        List<Garantia> garantias = garantiaRepository.findAll();

        int totalProductos = productos.size();

        // Stock neto real calculado dinámicamente de hardware disponible
        int stockNetoReal = productos.stream()
                .mapToInt(Producto::getStockEfectivo)
                .sum();

        int productosDisponibles = (int) productos.stream()
                .filter(p -> p.getStockEfectivo() > 0 && Boolean.TRUE.equals(p.getActivo()))
                .count();

        int productosAgotados = (int) productos.stream()
                .filter(p -> p.getStockEfectivo() == 0)
                .count();

        int unidadesVendidas = (int) items.stream()
                .filter(i -> i.getEstadoDispositivo() == EstadoDispositivo.Vendido)
                .count();

        int unidadesDisponibles = (int) items.stream()
                .filter(i -> i.getEstadoDispositivo() == EstadoDispositivo.Disponible)
                .count();

        // Ingresos estimados: suma del precio de los items vendidos
        double ingresosEstimados = items.stream()
                .filter(i -> i.getEstadoDispositivo() == EstadoDispositivo.Vendido && i.getProducto() != null)
                .mapToDouble(i -> i.getProducto().getPrecio())
                .sum();

        int garantiasVigentes = (int) garantias.stream()
                .filter(g -> g.getEliminadoAt() == null && g.getFechaCaducidad() != null && g.getFechaCaducidad().isAfter(ahora))
                .count();

        int garantiasAnuladas = (int) garantias.stream()
                .filter(g -> g.getEliminadoAt() != null)
                .count();

        double tasaRotacion = (unidadesVendidas + unidadesDisponibles) > 0
                ? ((double) unidadesVendidas / (unidadesVendidas + unidadesDisponibles)) * 100.0
                : 0.0;

        return DashboardMetricsDTO.builder()
                .stockNetoReal(stockNetoReal)
                .unidadesVendidas(unidadesVendidas)
                .unidadesDisponibles(unidadesDisponibles)
                .ingresosEstimados(ingresosEstimados)
                .productosDisponibles(productosDisponibles)
                .productosAgotados(productosAgotados)
                .totalProductos(totalProductos)
                .garantiasVigentes(garantiasVigentes)
                .garantiasAnuladas(garantiasAnuladas)
                .tasaRotacion(Math.round(tasaRotacion * 10.0) / 10.0)
                .build();
    }
}