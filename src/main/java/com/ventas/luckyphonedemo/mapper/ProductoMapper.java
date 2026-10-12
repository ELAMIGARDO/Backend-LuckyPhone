package com.ventas.luckyphonedemo.mapper;

import com.ventas.luckyphonedemo.dto.ProductoRequestDTO;
import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;

@Component
public class ProductoMapper {

    @Value("${luckyphone.whatsapp.numero:51912345678}")
    private String whatsappNumero;

    public ProductoResponseDTO toDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();

        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setMarca(producto.getMarca());
        dto.setModelo(producto.getModelo());
        dto.setAlmacenamiento(producto.getAlmacenamiento());
        dto.setRam(producto.getRam());
        dto.setColor(producto.getColor());
        dto.setEstado(producto.getEstado());
        dto.setBateria(producto.getBateria());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());
        dto.setImei1(producto.getImei1());
        dto.setImei2(producto.getImei2());
        dto.setNumeroSerial(producto.getNumeroSerial());
        
        if (producto.getCategoria() != null) {
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        String mensaje = "Hola LuckyPhone, estoy interesado en el siguiente equipo de tu catalogo:\n\n" +
                "Equipo: " + producto.getNombre() + " (" + producto.getMarca() + " " + producto.getModelo() + ")\n" +
                "Capacidad: " + producto.getAlmacenamiento() + " / RAM: " + producto.getRam() + "\n" +
                "Color: " + producto.getColor() + "\n" +
                "Estado: " + producto.getEstado() + " (Bateria: " + producto.getBateria() + ")\n" +
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
        producto.setBateria(dto.getBateria());
        producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        } else {
            producto.setActivo(true);
        }
        producto.setImei1(dto.getImei1());
        producto.setImei2(dto.getImei2());
        producto.setNumeroSerial(dto.getNumeroSerial());
        producto.setCategoria(categoria);
        return producto;
    }
}
