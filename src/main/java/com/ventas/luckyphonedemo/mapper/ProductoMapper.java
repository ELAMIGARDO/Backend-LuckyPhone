package com.ventas.luckyphonedemo.mapper;

import com.ventas.luckyphonedemo.dto.ItemCelularDTO;
import com.ventas.luckyphonedemo.dto.ProductoRequestDTO;
import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductoMapper {

    @Value("${luckyphone.whatsapp.numero:51912345678}")
    private String whatsappNumero;

    public ItemCelularDTO toItemDTO(ItemCelular item) {
        if (item == null) return null;
        return ItemCelularDTO.builder()
                .id(item.getId())
                .productoId(item.getProducto() != null ? item.getProducto().getId() : null)
                .imei1(item.getImei1())
                .imei2(item.getImei2())
                .numeroSerial(item.getNumeroSerial())
                .bateria(item.getBateria())
                .estadoDispositivo(item.getEstadoDispositivo())
                .creadoAt(item.getCreadoAt())
                .actualizadoAt(item.getActualizadoAt())
                .build();
    }

    public ItemCelular toItemEntity(ItemCelularDTO dto, Producto producto) {
        if (dto == null) return null;
        ItemCelular item = new ItemCelular();
        item.setId(dto.getId());
        item.setProducto(producto);
        item.setImei1(dto.getImei1());
        item.setImei2(dto.getImei2());
        item.setNumeroSerial(dto.getNumeroSerial());
        item.setBateria(dto.getBateria());
        if (dto.getEstadoDispositivo() != null) {
            item.setEstadoDispositivo(dto.getEstadoDispositivo());
        }
        return item;
    }

    public ProductoResponseDTO toDTO(Producto producto) {
        if (producto == null) return null;

        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStockEfectivo());
        dto.setMarca(producto.getMarca());
        dto.setModelo(producto.getModelo());
        dto.setAlmacenamiento(producto.getAlmacenamiento());
        dto.setRam(producto.getRam());
        dto.setColor(producto.getColor());
        dto.setEstado(producto.getEstado());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());

        if (producto.getCategoria() != null) {
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
            dto.setCategoriaId(producto.getCategoria().getId());
        }

        if (producto.getItemsCelulares() != null && !producto.getItemsCelulares().isEmpty()) {
            dto.setItemsCelulares(producto.getItemsCelulares().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setItemsCelulares(new ArrayList<>());
        }

        String mensaje = "Hola LuckyPhone, estoy interesado en el siguiente equipo de tu catalogo:\n\n" +
                "Equipo: " + producto.getNombre() + " (" + (producto.getMarca() != null ? producto.getMarca() : "") + " " + (producto.getModelo() != null ? producto.getModelo() : "") + ")\n" +
                "Capacidad: " + (producto.getAlmacenamiento() != null ? producto.getAlmacenamiento() : "N/A") + " / RAM: " + (producto.getRam() != null ? producto.getRam() : "N/A") + "\n" +
                "Color: " + (producto.getColor() != null ? producto.getColor() : "N/A") + "\n" +
                "Estado: " + (producto.getEstado() != null ? producto.getEstado() : "Disponible") + "\n" +
                "Precio: S/ " + producto.getPrecio() + "\n\n" +
                "Tienen stock disponible para concretar la compra?";

        String mensajeCodificado = UriUtils.encode(mensaje, StandardCharsets.UTF_8);
        String urlFinal = "https://wa.me/" + whatsappNumero + "?text=" + mensajeCodificado;

        dto.setWhatsappLink(urlFinal);

        return dto;
    }

    public Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setMarca(dto.getMarca());
        producto.setModelo(dto.getModelo());
        producto.setAlmacenamiento(dto.getAlmacenamiento());
        producto.setRam(dto.getRam());
        producto.setColor(dto.getColor());
        producto.setEstado(dto.getEstado());
        producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        } else {
            producto.setActivo(true);
        }
        producto.setCategoria(categoria);
        return producto;
    }
}
