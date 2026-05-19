package com.ventas.luckyphonedemo.mapper;

import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.model.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;

@Component
public class ProductoMapper {

    // CORREGIDO: Asegúrate de tener declarada exactamente esta variable aquí arriba
    @Value("${luckyphone.whatsapp.numero}")
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

        // Redactar el mensaje comercial utilizando la ficha técnica del celular
        String mensaje = "Hola LuckyPhone, estoy interesado en el siguiente equipo de tu catálogo:\n\n" +
                "📱 Equipo: " + producto.getNombre() + " (" + producto.getMarca() + " " + producto.getModelo() + ")\n" +
                "💾 Capacidad: " + producto.getAlmacenamiento() + " / RAM: " + producto.getRam() + "\n" +
                "🎨 Color: " + producto.getColor() + "\n" +
                "✨ Estado: " + producto.getEstado() + " (Batería: " + producto.getBateria() + ")\n" +
                "💰 Precio: S/ " + producto.getPrecio() + "\n\n" +
                "¿Tienen stock disponible para concretar la compra?";

        // Codificar el texto para la URL
        String mensajeCodificado = UriUtils.encode(mensaje, StandardCharsets.UTF_8);

        // Construir la URL final dinámicamente usando la variable inyectada
        String urlFinal = "https://wa.me/" + whatsappNumero + "?text=" + mensajeCodificado;

        dto.setWhatsappLink(urlFinal);

        return dto;
    }
}