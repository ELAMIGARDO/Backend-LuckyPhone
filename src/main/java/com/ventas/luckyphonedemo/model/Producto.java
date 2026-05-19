package com.ventas.luckyphonedemo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Table(name = "producto")
@NoArgsConstructor
@AllArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    private double precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    // --- ESPECIFICACIONES TÉCNICAS (Lo que el Admin llenará) ---

    private String marca;          // Ej: Apple, Samsung (Viene de Categoria, pero puedes repetirlo aquí para texto rápido)
    private String modelo;         // Ej: "iPhone 15 Pro Max"
    private String almacenamiento; // Ej: "128GB", "256GB", "512GB"
    private String ram;            // Ej: "6GB", "8GB", "12GB"
    private String color;          // Ej: "Negro Espacial", "Titanio Natural"
    private String estado;         // Ej: "Nuevo", "Seminuevo", "Open Box"
    private String bateria;        // Ej: "100%" o "5000 mAh" (Clave en celulares)

    // --- CONTROL DE CATÁLOGO ---

    private boolean activo = true; // Para ocultar productos sin borrarlos

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String imagenUrl;
}