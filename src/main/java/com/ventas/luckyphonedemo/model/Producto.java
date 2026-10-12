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

    // --- ESPECIFICACIONES TECNICAS ---
    private String marca;
    private String modelo;
    private String almacenamiento;
    private String ram;
    private String color;
    private String estado;
    private String bateria;

    @Lob
    @Column(name = "imagen_url", columnDefinition = "LONGTEXT")
    private String imagenUrl;

    private Boolean activo = true;

    // --- IDENTIFICADORES OPCIONALES (NULL para cargadores/accesorios) ---
    @Column(name = "imei_1", length = 20, nullable = true)
    private String imei1;

    @Column(name = "imei_2", length = 20, nullable = true)
    private String imei2;

    @Column(name = "numero_serial", length = 50, nullable = true)
    private String numeroSerial;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}