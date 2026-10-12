package com.ventas.luckyphonedemo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    private double precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    private String marca;
    private String modelo;
    private String almacenamiento;
    private String ram;
    private String color;
    private String estado;

    @NotNull(message = "La categoria es obligatoria")
    private Long categoriaId;

    private String imagenUrl;
    private Boolean activo;

    /**
     * Opcional: Lista de items de celulares que se pueden dar de alta al registrar o actualizar el producto.
     */
    private List<ItemCelularDTO> itemsCelulares;
}
