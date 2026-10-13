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
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public List<String> obtenerColoresDisponibles() {
        return repository.findDistinctColores();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerDisponibles() {
        return repository.findDisponiblesActivos().stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerAgotados() {
        return repository.findAgotados().stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    // POST con DTO: Control estricto de unicidad de Producto Padre por Modelo + Color + Almacenamiento + Estado
    @Transactional(rollbackFor = Exception.class)
    public ProductoResponseDTO crearConDto(ProductoRequestDTO dto) {
        if (dto.getStock() < 0) {
            throw new BadRequestException("El Stock no puede ser negativo");
        }

        // Sanitización estricta de cadenas de texto
        if (dto.getColor() != null) {
            dto.setColor(dto.getColor().trim());
        }
        if (dto.getModelo() != null) {
            dto.setModelo(dto.getModelo().trim());
        }
        if (dto.getNombre() != null) {
            dto.setNombre(dto.getNombre().trim());
        }
        if (dto.getEstado() != null) {
            dto.setEstado(dto.getEstado().trim());
        }
        if (dto.getAlmacenamiento() != null) {
            dto.setAlmacenamiento(dto.getAlmacenamiento().trim());
        }

        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + dto.getCategoriaId()));
        }

        // 1. REGLA DE IDENTIDAD DEL PRODUCTO PADRE:
        // Verificar si ya existe un padre que coincida exactamente en Modelo, Color, Almacenamiento y Estado (Condición)
        boolean esCelular = (categoria != null && "Celulares".equalsIgnoreCase(categoria.getNombre()))
                || (dto.getItemsCelulares() != null && !dto.getItemsCelulares().isEmpty());

        Producto productoPadre;
        if (esCelular && dto.getModelo() != null && dto.getColor() != null && dto.getEstado() != null) {
            Optional<Producto> existenteOpt = repository.findByModeloIgnoreCaseAndColorIgnoreCaseAndAlmacenamientoIgnoreCaseAndEstadoIgnoreCase(
                    dto.getModelo().trim(),
                    dto.getColor().trim(),
                    dto.getAlmacenamiento() != null ? dto.getAlmacenamiento().trim() : "",
                    dto.getEstado().trim()
            );

            if (existenteOpt.isPresent()) {
                // CASO SINTONÍA TOTAL: Reutilizamos el Padre existente y solo asociamos los nuevos items hijos
                productoPadre = existenteOpt.get();
                // Si el precio viene especificado, se actualiza
                if (dto.getPrecio() > 0) {
                    productoPadre.setPrecio(dto.getPrecio());
                }
            } else {
                // CASO CONDICIÓN DIFERENTE O NUEVO MODELO: Creamos una entidad Producto Padre independiente
                productoPadre = productoMapper.toEntity(dto, categoria);
            }
        } else {
            productoPadre = productoMapper.toEntity(dto, categoria);
        }

        // 2. Asociar los nuevos items físicos de celular si aplican
        if (dto.getItemsCelulares() != null && !dto.getItemsCelulares().isEmpty()) {
            for (ItemCelularDTO itemDto : dto.getItemsCelulares()) {
                if (itemCelularRepository.existsByImei1(itemDto.getImei1())) {
                    throw new BadRequestException("Ya existe un celular registrado con el IMEI: " + itemDto.getImei1());
                }
                ItemCelular item = productoMapper.toItemEntity(itemDto, productoPadre);
                if (item.getEstadoDispositivo() == null) {
                    item.setEstadoDispositivo(EstadoDispositivo.Disponible);
                }
                productoPadre.addItemCelular(item);
            }
        }

        Producto guardado = repository.save(productoPadre);
        return productoMapper.toDTO(guardado);
    }

    // PUT con DTO
    @Transactional
    public ProductoResponseDTO actualizarConDto(Long id, ProductoRequestDTO dto) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Sanitización estricta de cadenas de texto
        if (dto.getColor() != null) {
            dto.setColor(dto.getColor().trim());
        }
        if (dto.getModelo() != null) {
            dto.setModelo(dto.getModelo().trim());
        }
        if (dto.getNombre() != null) {
            dto.setNombre(dto.getNombre().trim());
        }
        if (dto.getEstado() != null) {
            dto.setEstado(dto.getEstado().trim());
        }
        if (dto.getAlmacenamiento() != null) {
            dto.setAlmacenamiento(dto.getAlmacenamiento().trim());
        }

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

        // Si se especifican items para agregar o editar
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
        if (productoActualizado.getColor() != null) {
            producto.setColor(productoActualizado.getColor().trim());
        }
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
        if (producto.getColor() != null) {
            producto.setColor(producto.getColor().trim());
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