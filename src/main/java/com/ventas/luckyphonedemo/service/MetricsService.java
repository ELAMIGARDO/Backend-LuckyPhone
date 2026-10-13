package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.MetricsCountsDTO;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.Garantia;
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
public class MetricsService {

    private final ProductoRepository productoRepository;
    private final GarantiaRepository garantiaRepository;
    private final ItemCelularRepository itemCelularRepository;

    @Transactional(readOnly = true)
    public MetricsCountsDTO obtenerConteosGlobales() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Garantia> garantias = garantiaRepository.findAll();

        long countVigentes = garantias.stream()
                .filter(g -> g.getEliminadoAt() == null && g.getFechaCaducidad() != null && g.getFechaCaducidad().isAfter(ahora))
                .count();

        long countCaducados = garantias.stream()
                .filter(g -> g.getEliminadoAt() == null && (g.getFechaCaducidad() == null || !g.getFechaCaducidad().isAfter(ahora)))
                .count();

        long countAnulados = garantias.stream()
                .filter(g -> g.getEliminadoAt() != null)
                .count();

        long countTotalesGarantias = garantias.stream()
                .filter(g -> g.getEliminadoAt() == null)
                .count();

        long disponibles = productoRepository.countDisponiblesActivos();
        long agotados = productoRepository.countAgotados();
        long totalProductos = productoRepository.count();

        long itemsVendidos = itemCelularRepository.countByEstadoDispositivo(EstadoDispositivo.Vendido);
        long itemsDisponibles = itemCelularRepository.countByEstadoDispositivo(EstadoDispositivo.Disponible);

        return MetricsCountsDTO.builder()
                .disponibles(disponibles)
                .agotados(agotados)
                .anulados(countAnulados)
                .vigentes(countVigentes)
                .caducados(countCaducados)
                .totales(countTotalesGarantias)
                .totalProductos(totalProductos)
                .itemsVendidos(itemsVendidos)
                .itemsDisponibles(itemsDisponibles)
                .build();
    }
}