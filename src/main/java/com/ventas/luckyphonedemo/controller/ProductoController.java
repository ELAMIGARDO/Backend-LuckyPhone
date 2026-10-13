package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.dto.ProductoRequestDTO;
import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.mapper.ProductoMapper;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/products", "/api/productos"})
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;
    private final ProductoMapper productoMapper;

    // ==========================================
    // ENDPOINTS PUBLICOS (Para los Clientes - Catalogo WSP)
    // ==========================================

    @GetMapping("/lista")
    public ResponseEntity<Page<ProductoResponseDTO>> obtenerCatalogoPublico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.obtenerCatalogoPublico(page, size));
    }

    @GetMapping("/colores-disponibles")
    public ResponseEntity<List<String>> obtenerColoresDisponibles() {
        return ResponseEntity.ok(service.obtenerColoresDisponibles());
    }

    @GetMapping("/admin/buscar")
    public ResponseEntity<List<Producto>> buscarProductoInterno(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductoPublico(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarEnCatalogoPublico(nombre));
    }

    // ==========================================
    // ENDPOINTS DE ADMINISTRACION (Para el Panel Interno)
    // ==========================================
    @GetMapping("/lista-completa")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductos() {
        return ResponseEntity.ok(service.obtenerTodosResponseDTO());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerDisponibles() {
        return ResponseEntity.ok(service.obtenerDisponibles());
    }

    @GetMapping("/agotados")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerAgotados() {
        return ResponseEntity.ok(service.obtenerAgotados());
    }

    @GetMapping
    public ResponseEntity<Page<Producto>> obtenerProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.obtenerTodosPaginado(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id){
        Producto producto = service.obtenerPorId(id);
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> obtenerPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoria(categoriaId));
    }

    // POST - Crear producto
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO productoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearConDto(productoDto));
    }

    // PUT - Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO productoDto) {
        return ResponseEntity.ok(service.actualizarConDto(id, productoDto));
    }

    // DELETE - Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH - Cambiar estado de visibilidad rapido (Activo/Inactivo)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Producto> cambiarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }
}