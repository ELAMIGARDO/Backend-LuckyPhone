package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.ProductoRequestDTO;
import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.mapper.ProductoMapper;
import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.CategoriaRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoService productoService;

    private ProductoRequestDTO requestDTO;
    private Producto producto;
    private Categoria categoria;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Celulares", null);

        requestDTO = ProductoRequestDTO.builder()
                .nombre("iPhone 15 Pro")
                .descripcion("Celular gama alta")
                .precio(4500.0)
                .stock(10)
                .marca("Apple")
                .modelo("iPhone 15 Pro")
                .categoriaId(1L)
                .build();

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("iPhone 15 Pro");
        producto.setPrecio(4500.0);
        producto.setStock(10);
        producto.setCategoria(categoria);
        producto.setActivo(true);

        responseDTO = new ProductoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("iPhone 15 Pro");
        responseDTO.setPrecio(4500.0);
    }

    @Test
    void testCrearProductoConDtoExitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoMapper.toEntity(requestDTO, categoria)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO resultado = productoService.crearConDto(requestDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("iPhone 15 Pro", resultado.getNombre());
        verify(productoRepository).save(producto);
    }

    @Test
    void testCrearProductoConStockNegativo_LanzaBadRequestException() {
        requestDTO.setStock(-5);

        assertThrows(BadRequestException.class, () -> productoService.crearConDto(requestDTO));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void testObtenerPorId_NoExiste_LanzaResourceNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerPorId(99L));
    }
}
