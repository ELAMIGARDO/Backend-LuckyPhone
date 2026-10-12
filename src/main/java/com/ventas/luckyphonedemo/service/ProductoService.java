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
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoMapper productoMapper;

    // ==========================================
    // METODOS PARA EL CATALOGO PUBLICO
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
    // METODOS DE CONSULTA Y GESTION
    // ==========================================

    public List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    public List<Producto> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    // POST con DTO
    public ProductoResponseDTO crearConDto(ProductoRequestDTO dto) {
        if (dto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }
        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + dto.getCategoriaId()));
        }
        Producto producto = productoMapper.toEntity(dto, categoria);
        Producto guardado = repository.save(producto);
        return productoMapper.toDTO(guardado);
    }

    // PUT con DTO
    public ProductoResponseDTO actualizarConDto(Long id, ProductoRequestDTO dto) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + dto.getCategoriaId()));
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
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }
        producto.setImei1(dto.getImei1());
        producto.setImei2(dto.getImei2());
        producto.setNumeroSerial(dto.getNumeroSerial());

        Producto actualizado = repository.save(producto);
        return productoMapper.toDTO(actualizado);
    }

    // PUT (Actualizar) legacy para compatibilidad
    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setStock(productoActualizado.getStock());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setCategoria(productoActualizado.getCategoria());

        producto.setAlmacenamiento(productoActualizado.getAlmacenamiento());
        producto.setRam(productoActualizado.getRam());
        producto.setColor(productoActualizado.getColor());
        producto.setEstado(productoActualizado.getEstado());
        producto.setBateria(productoActualizado.getBateria());
        producto.setImei1(productoActualizado.getImei1());
        producto.setImei2(productoActualizado.getImei2());
        producto.setNumeroSerial(productoActualizado.getNumeroSerial());
        
        if (productoActualizado.getActivo() != null) {
            producto.setActivo(productoActualizado.getActivo());
        }

        return repository.save(producto);
    }

    public Producto crear(Producto producto) {
        if (producto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }
        return repository.save(producto);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("No se puede eliminar, el producto no existe");
        repository.deleteById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    public Page<Producto> obtenerTodosPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Producto cambiarEstado(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        boolean estadoActual = Boolean.TRUE.equals(producto.getActivo());
        producto.setActivo(!estadoActual);

        return repository.save(producto);
    }
}
