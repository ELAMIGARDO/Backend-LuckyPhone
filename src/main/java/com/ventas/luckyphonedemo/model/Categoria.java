package com.ventas.luckyphonedemo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Entity
@Table(name = "categoria")
@AllArgsConstructor
@NoArgsConstructor

public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    private String nombre;

    // Relación inversa: Una categoría tiene muchos productos
    @OneToMany(mappedBy = "categoria" )
    @JsonIgnore // Evita errores de bucle infinito al devolver el JSON
    private List<Producto> productos;
}
