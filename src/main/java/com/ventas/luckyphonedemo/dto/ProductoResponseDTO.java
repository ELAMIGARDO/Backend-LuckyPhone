package com.ventas.luckyphonedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
    private String bateria;
    private String imagenUrl;
    private String whatsappLink; // El link autogenerado

}
