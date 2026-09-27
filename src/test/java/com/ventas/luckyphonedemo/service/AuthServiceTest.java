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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegistroRequestDTO registroDTO;
    private Cliente cliente;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        registroDTO = RegistroRequestDTO.builder()
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .telefono("987654321")
                .email("juan@gmail.com")
                .password("password123")
                .rol("ROLE_USER")
                .aceptoTerminos(true)
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .telefono("987654321")
                .email("juan@gmail.com")
                .password("encoded_password")
                .rol("ROLE_USER")
                .build();

        clienteResponseDTO = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan@gmail.com")
                .rol("ROLE_USER")
                .build();
    }

    @Test
    void testRegistrarClienteExitoso() {
        when(clienteRepository.existsByEmail(registroDTO.getEmail())).thenReturn(false);
        when(clienteRepository.existsByDni(registroDTO.getDni())).thenReturn(false);
        when(passwordEncoder.encode(registroDTO.getPassword())).thenReturn("encoded_password");
        when(clienteMapper.toEntity(any(), any())).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDTO(cliente)).thenReturn(clienteResponseDTO);

        ClienteResponseDTO resultado = authService.registrar(registroDTO);

        assertNotNull(resultado);
        assertEquals("juan@gmail.com", resultado.getEmail());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void testRegistrarCliente_EmailExistente_LanzaBadRequestException() {
        when(clienteRepository.existsByEmail(registroDTO.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registrar(registroDTO));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testLoginExitoso_RetornaAuthResponseDTO() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("juan@gmail.com", "password123");
        Authentication authMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(clienteRepository.findByEmail("juan@gmail.com")).thenReturn(Optional.of(cliente));
        when(tokenProvider.generarToken(authMock, "ROLE_USER")).thenReturn("fake-jwt-token");

        AuthResponseDTO response = authService.login(loginDTO);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTipoToken());
        assertEquals("juan@gmail.com", response.getEmail());
    }
}
