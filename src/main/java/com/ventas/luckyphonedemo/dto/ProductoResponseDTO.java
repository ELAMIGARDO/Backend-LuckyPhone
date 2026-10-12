package com.ventas.luckyphonedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String marca;
    private String modelo;
    private String almacenamiento;
    private String ram;
    private String color;
    private String estado;
    private String imagenUrl;
    private Boolean activo;
    private String whatsappLink;
    private String categoriaNombre;
    private Long categoriaId;
    private List<ItemCelularDTO> itemsCelulares;
}
