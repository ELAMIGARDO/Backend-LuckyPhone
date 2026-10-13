package com.ventas.luckyphonedemo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa una unidad física específica de un celular en tienda o vitrina.
 * Trazabilidad única por IMEI 1, IMEI 2, Número de Serie y Salud de Batería.
 */
@Entity
@Table(name = "item_celular")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "producto")
@EqualsAndHashCode(of = "id")
public class ItemCelular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Producto producto;

    @NotBlank(message = "El IMEI 1 es obligatorio para un teléfono físico")
    @Size(min = 14, max = 20, message = "El IMEI 1 debe tener entre 14 y 20 caracteres")
    @Column(name = "imei_1", length = 20, nullable = false, unique = true)
    private String imei1;

    @Column(name = "imei_2", length = 20, nullable = true)
    private String imei2;

    @Column(name = "numero_serial", length = 50, nullable = true)
    private String numeroSerial;

    @Column(name = "bateria", length = 10, nullable = true)
    private String bateria; // Representa el porcentaje, ej: "88%"

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_dispositivo", nullable = false, length = 20)
    @Builder.Default
    private EstadoDispositivo estadoDispositivo = EstadoDispositivo.Disponible;

    @Column(name = "creado_at", updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at")
    private LocalDateTime actualizadoAt;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @PrePersist
    protected void onCreate() {
        this.creadoAt = LocalDateTime.now();
        this.actualizadoAt = LocalDateTime.now();
        if (this.estadoDispositivo == null) {
            this.estadoDispositivo = EstadoDispositivo.Disponible;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoAt = LocalDateTime.now();
    }
}
