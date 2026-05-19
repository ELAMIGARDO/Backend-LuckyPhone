package com.ventas.luckyphonedemo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
@Entity
@Table(name = "cliente")
@AllArgsConstructor
@NoArgsConstructor

public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "DNI obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Column(unique = true, length = 8)
    private String dni;

    @NotBlank(message = "El número telefónico es obligatorio")
    private String telefono;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    @Column(unique = true)
    private String email;

    private String direccion;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

}
