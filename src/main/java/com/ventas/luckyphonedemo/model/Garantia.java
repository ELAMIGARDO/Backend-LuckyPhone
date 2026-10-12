package com.ventas.luckyphonedemo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "garantias")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garantia {

    @Id
    @Column(name = "id_garantia", length = 50)
    private String id;

    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(name = "cliente_dni", nullable = false, length = 20)
    private String clienteDni;

    @Column(name = "cliente_telefono", length = 20)
    private String clienteTelefono;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "producto_modelo", nullable = false)
    private String productoModelo;

    // Identificadores Opcionales (Permiten NULL)
    @Column(name = "imei1", length = 20)
    private String imei1;

    @Column(name = "imei2", length = 20)
    private String imei2;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(name = "descuento_monto")
    private Double descuentoMonto = 0.0;

    @Column(name = "precio_final", nullable = false)
    private Double precioFinal;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDateTime fechaCaducidad;

    @Column(name = "duracion_garantia", nullable = false, length = 20)
    private String duracionGarantia;

    @Column(name = "emisor_nombre", nullable = false, length = 150)
    private String emisorNombre;

    // Estado del ciclo transaccional de pago de la proforma
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    // Borrado Logico (Soft Delete / Anulacion)
    @Column(name = "eliminado_at")
    private LocalDateTime eliminadoAt;
}
