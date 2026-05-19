package com.ventas.luckyphonedemo.controller;

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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;
    private final ProductoMapper ProductoMapper;

    // ==========================================
    // 🌐 ENDPOINTS PÚBLICOS (Para los Clientes - Catálogo WSP)
    // ==========================================

    /**
     * Catálogo principal público paginado.
     * Muestra solo celulares activos y adjunta el link de WhatsApp.
     * URL: GET /api/productos/catalogo
     */
    @GetMapping("/lista")
    public ResponseEntity<Page<ProductoResponseDTO>> obtenerCatalogoPublico(
          @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.obtenerCatalogoPublico(page, size));
    }
    @GetMapping("/admin/buscar")
    public ResponseEntity<List<Producto>> buscarProductoInterno(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombre(nombre));
    }
    /**
     * Buscador inteligente para el catálogo público.
     * Filtra por nombre y devuelve ProductoResponseDTO (evita el choque de tipos).
     * URL: GET /api/productos/buscar
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductoPublico(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarEnCatalogoPublico(nombre));
    }

    // ==========================================
    // 🔐 ENDPOINTS DE ADMINISTRACIÓN (Para el Panel Interno)
    // ==========================================
    @GetMapping("/lista-completa")
    public ResponseEntity<List<Producto>> obtenerProductos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }
    // GET con Paginación interna para la tabla del Administrador
    @GetMapping
    public ResponseEntity<Page<Producto>> obtenerProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.obtenerTodosPaginado(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id){
        // Opción A: Si tu servicio ya tiene un método que devuelve el DTO directo:
        // return ResponseEntity.ok(service.obtenerDetallePublico(id));

        // Opción B: Si tu servicio devuelve el Producto y usas el mapper aquí (Asegúrate de tener inyectado tu ProductoMapper):
        Producto producto = service.obtenerPorId(id);
        return ResponseEntity.ok(ProductoMapper.toDTO(producto));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> obtenerPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoria(categoriaId));
    }

    // POST - Crear producto
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(producto));
    }

    // PUT - Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(service.actualizar(id, producto));
    }

    // DELETE - Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH - Cambiar estado de visibilidad rápido (Activo/Inactivo)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Producto> cambiarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(service.cambiarEstado(id));
    }
}