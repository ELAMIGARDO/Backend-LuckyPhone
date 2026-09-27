package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.AuthResponseDTO;
import com.ventas.luckyphonedemo.dto.ClienteResponseDTO;
import com.ventas.luckyphonedemo.dto.LoginRequestDTO;
import com.ventas.luckyphonedemo.dto.RegistroRequestDTO;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.mapper.ClienteMapper;
import com.ventas.luckyphonedemo.model.Cliente;
import com.ventas.luckyphonedemo.repositorio.ClienteRepository;
import com.ventas.luckyphonedemo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public ClienteResponseDTO registrar(RegistroRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("El correo ya se encuentra registrado: " + dto.getEmail());
        }
        if (clienteRepository.existsByDni(dto.getDni())) {
            throw new BadRequestException("El DNI ya se encuentra registrado: " + dto.getDni());
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        Cliente cliente = clienteMapper.toEntity(dto, encodedPassword);
        Cliente guardado = clienteRepository.save(cliente);

        return clienteMapper.toDTO(guardado);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        Cliente cliente = clienteRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        String token = tokenProvider.generarToken(authentication, cliente.getRol());

        return AuthResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .email(cliente.getEmail())
                .rol(cliente.getRol())
                .build();
    }
}
