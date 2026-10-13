package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.dto.ItemCelularDTO;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.mapper.ProductoMapper;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items-celulares")
@CrossOrigin(origins = "*")
public class ItemCelularController {

    @Autowired
    private ItemCelularRepository itemCelularRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoMapper productoMapper;

    /**
     * GET /api/items-celulares/disponibles?productoId=1
     * Lista los celulares disponibles para emitir proformas/garantías
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<ItemCelularDTO>> listarDisponibles(
            @RequestParam(required = false) Long productoId) {
        List<ItemCelular> items;
        if (productoId != null) {
            items = itemCelularRepository.findByProductoIdAndEstadoDispositivo(productoId, EstadoDispositivo.Disponible);
        } else {
            items = itemCelularRepository.findAll().stream()
                    .filter(i -> i.getEstadoDispositivo() == EstadoDispositivo.Disponible)
                    .collect(Collectors.toList());
        }
        List<ItemCelularDTO> dtos = items.stream().map(productoMapper::toItemDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/items-celulares
     * Registrar una unidad individual de celular para un producto existente
     */
    @PostMapping
    public ResponseEntity<ItemCelularDTO> agregarItem(@Valid @RequestBody ItemCelularDTO dto) {
        if (dto.getProductoId() == null) {
            throw new BadRequestException("El ID del producto es obligatorio");
        }
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getProductoId()));

        if (itemCelularRepository.existsByImei1(dto.getImei1())) {
            throw new BadRequestException("Ya existe una unidad registrada con el IMEI: " + dto.getImei1());
        }

        ItemCelular item = productoMapper.toItemEntity(dto, producto);
        if (item.getEstadoDispositivo() == null) {
            item.setEstadoDispositivo(EstadoDispositivo.Disponible);
        }
        ItemCelular guardado = itemCelularRepository.save(item);
        return ResponseEntity.ok(productoMapper.toItemDTO(guardado));
    }

    /**
     * PUT /api/items-celulares/{id}/estado
     * Cambiar estado de la unidad (Disponible, Reservado, Vendido)
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<ItemCelularDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoDispositivo estado) {
        ItemCelular item = itemCelularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item celular no encontrado"));
        item.setEstadoDispositivo(estado);
        if (estado == EstadoDispositivo.Vendido) {
            if (item.getFechaVenta() == null) {
                item.setFechaVenta(java.time.LocalDateTime.now());
            }
        } else {
            item.setFechaVenta(null);
        }
        ItemCelular actualizado = itemCelularRepository.save(item);
        return ResponseEntity.ok(productoMapper.toItemDTO(actualizado));
    }

    /**
     * DELETE /api/items-celulares/{id}
     * Eliminar unidad física si no ha sido vendida
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        ItemCelular item = itemCelularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item celular no encontrado"));
        if (item.getEstadoDispositivo() == EstadoDispositivo.Vendido) {
            throw new BadRequestException("No se puede eliminar un dispositivo que ya ha sido vendido");
        }
        itemCelularRepository.delete(item);
        return ResponseEntity.noContent().build();
    }
}
