package com.ventas.luckyphonedemo.mapper;

import com.ventas.luckyphonedemo.dto.ClienteResponseDTO;
import com.ventas.luckyphonedemo.dto.RegistroRequestDTO;
import com.ventas.luckyphonedemo.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toDTO(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .dni(cliente.getDni())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .rol(cliente.getRol())
                .build();
    }

    public Cliente toEntity(RegistroRequestDTO dto, String encodedPassword) {
        if (dto == null) return null;
        String rolFinal = (dto.getRol() != null && !dto.getRol().isBlank()) ? dto.getRol() : "ROLE_USER";
        return Cliente.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .direccion(dto.getDireccion())
                .password(encodedPassword)
                .rol(rolFinal)
                .build();
    }
}
