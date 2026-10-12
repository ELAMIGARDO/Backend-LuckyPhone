package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.dto.ItemCelularDTO;
import com.ventas.luckyphonedemo.dto.ProductoRequestDTO;
import com.ventas.luckyphonedemo.dto.ProductoResponseDTO;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.mapper.ProductoMapper;
import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.CategoriaRepository;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ItemCelularRepository itemCelularRepository;

    @Autowired
    private ProductoMapper productoMapper;

    // ==========================================
    // METODOS PARA EL CATALOGO PUBLICO
    // ==========================================

    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> obtenerCatalogoPublico(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosActivos = repository.findByActivoTrue(pageable);
        return productosActivos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarEnCatalogoPublico(String nombre) {
        List<Producto> productos = repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
        return productos.stream().map(productoMapper::toDTO).toList();
    }

    // ==========================================
    // METODOS DE CONSULTA Y GESTION
    // ==========================================

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    // POST con DTO
    @Transactional
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
        
        // Si vienen items de celular hijos
        if (dto.getItemsCelulares() != null && !dto.getItemsCelulares().isEmpty()) {
            for (ItemCelularDTO itemDto : dto.getItemsCelulares()) {
                if (itemCelularRepository.existsByImei1(itemDto.getImei1())) {
                    throw new BadRequestException("Ya existe un celular registrado con el IMEI: " + itemDto.getImei1());
                }
                ItemCelular item = productoMapper.toItemEntity(itemDto, producto);
                if (item.getEstadoDispositivo() == null) {
                    item.setEstadoDispositivo(EstadoDispositivo.Disponible);
                }
                producto.addItemCelular(item);
            }
        }

        Producto guardado = repository.save(producto);
        return productoMapper.toDTO(guardado);
    }

    // PUT con DTO
    @Transactional
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
        producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        // Si se especifican items para agregar
        if (dto.getItemsCelulares() != null && !dto.getItemsCelulares().isEmpty()) {
            for (ItemCelularDTO itemDto : dto.getItemsCelulares()) {
                if (itemDto.getId() == null) {
                    // Nuevo item a añadir
                    if (itemCelularRepository.existsByImei1(itemDto.getImei1())) {
                        throw new BadRequestException("Ya existe un celular con el IMEI: " + itemDto.getImei1());
                    }
                    ItemCelular newItem = productoMapper.toItemEntity(itemDto, producto);
                    if (newItem.getEstadoDispositivo() == null) {
                        newItem.setEstadoDispositivo(EstadoDispositivo.Disponible);
                    }
                    producto.addItemCelular(newItem);
                } else {
                    // Actualizar item existente
                    itemCelularRepository.findById(itemDto.getId()).ifPresent(existingItem -> {
                        existingItem.setImei1(itemDto.getImei1());
                        existingItem.setImei2(itemDto.getImei2());
                        existingItem.setNumeroSerial(itemDto.getNumeroSerial());
                        existingItem.setBateria(itemDto.getBateria());
                        if (itemDto.getEstadoDispositivo() != null) {
                            existingItem.setEstadoDispositivo(itemDto.getEstadoDispositivo());
                        }
                    });
                }
            }
        }

        Producto actualizado = repository.save(producto);
        return productoMapper.toDTO(actualizado);
    }

    // PUT legacy
    @Transactional
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
        
        if (productoActualizado.getActivo() != null) {
            producto.setActivo(productoActualizado.getActivo());
        }

        return repository.save(producto);
    }

    @Transactional
    public Producto crear(Producto producto) {
        if (producto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }
        return repository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("No se puede eliminar, el producto no existe");
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional(readOnly = true)
    public Page<Producto> obtenerTodosPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    @Transactional
    public Producto cambiarEstado(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        boolean estadoActual = Boolean.TRUE.equals(producto.getActivo());
        producto.setActivo(!estadoActual);

        return repository.save(producto);
    }
}
