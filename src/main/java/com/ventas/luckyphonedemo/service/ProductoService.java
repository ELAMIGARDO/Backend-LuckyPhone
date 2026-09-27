package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.mapper.ProductoMapper;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private ProductoMapper productoMapper;

    // ==========================================
    // 🌐 MÉTODOS NUEVOS PARA EL CATÁLOGO PÚBLICO
    // ==========================================

    public Page<ProductoResponseDTO> obtenerCatalogoPublico(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosActivos = repository.findByActivoTrue(pageable);
        return productosActivos.map(productoMapper::toDTO);
    }

    public List<ProductoResponseDTO> buscarEnCatalogoPublico(String nombre) {
        List<Producto> productos = repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
        return productos.stream().map(productoMapper::toDTO).toList();
    }

    // ==========================================
    // 📋 MÉTODOS ANTERIORES MANTENIDOS
    // ==========================================

    // GET
    public List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    //GET POR ID  //RuntimeException: dice que crear un excepción personalizada llamada ResourceNotFoundException para que el error sea más específico.
    public Producto obtenerPorId(Long id){
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    //Get por categoria id
    public List<Producto> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    @Autowired
    private com.ventas.luckyphonedemo.repositorio.CategoriaRepository categoriaRepository;

    // POST con DTO
    public ProductoResponseDTO crearConDto(com.ventas.luckyphonedemo.dto.ProductoRequestDTO dto) {
        if (dto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }
        com.ventas.luckyphonedemo.model.Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.getCategoriaId()));
        }
        Producto producto = productoMapper.toEntity(dto, categoria);
        Producto guardado = repository.save(producto);
        return productoMapper.toDTO(guardado);
    }

    // PUT con DTO
    public ProductoResponseDTO actualizarConDto(Long id, com.ventas.luckyphonedemo.dto.ProductoRequestDTO dto) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (dto.getCategoriaId() != null) {
            com.ventas.luckyphonedemo.model.Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }

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

        Producto actualizado = repository.save(producto);
        return productoMapper.toDTO(actualizado);
    }

    // PUT (Actualizar) legacy manteniéndose para compatibilidad
    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Campos básicos
        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setStock(productoActualizado.getStock());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setCategoria(productoActualizado.getCategoria());

        // --- NUEVOS campos para el Administrador ---
        producto.setAlmacenamiento(productoActualizado.getAlmacenamiento());
        producto.setRam(productoActualizado.getRam());
        producto.setColor(productoActualizado.getColor());
        producto.setEstado(productoActualizado.getEstado());
        producto.setBateria(productoActualizado.getBateria());
        producto.setActivo(productoActualizado.isActivo());

        return repository.save(producto);
    }

    //Post (crear)
    public Producto crear(Producto producto) {
        if (producto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }
        return repository.save(producto);
    }

    // DELETE
    public void eliminar(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("No se puede eliminar, el producto no existe");
        repository.deleteById(id);
    }

    // TU BUSCADOR ORIGINAL RECUPERADO
    // Buscar por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    // Obtener todos con PAGINACIÓN
    // El "page" es el número de página y "size" cuántos productos mostrar
    public Page<Producto> obtenerTodosPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Producto cambiarEstado(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Si estaba true, pasa a false. Si estaba false, pasa a true.
        producto.setActivo(!producto.isActivo());

        return repository.save(producto);
    }
}