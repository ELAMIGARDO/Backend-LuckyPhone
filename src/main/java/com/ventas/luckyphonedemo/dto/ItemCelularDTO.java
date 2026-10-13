package com.ventas.luckyphonedemo.dto;

import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemCelularDTO {

    private Long id;

    private Long productoId;

    @NotBlank(message = "El IMEI 1 es obligatorio")
    private String imei1;

    private String imei2;

    private String numeroSerial;

    private String bateria;

    private EstadoDispositivo estadoDispositivo;

    private LocalDateTime creadoAt;

    private LocalDateTime actualizadoAt;

    private LocalDateTime fechaVenta;
}
