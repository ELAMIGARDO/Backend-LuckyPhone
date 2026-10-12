package com.ventas.luckyphonedemo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el modelo comercial del catálogo.
 * Para 'Celulares', el stock se calcula dinámicamente según sus items 'Disponible'.
 * Para 'Accesorios/Cargadores', usa el campo físico 'stock'.
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"itemsCelulares", "categoria"})
@EqualsAndHashCode(of = "id")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    private double precio;

    /**
     * Stock físico almacenado manualmente (utilizado para categorías Accesorios/Cargadores).
     */
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    @Builder.Default
    private int stock = 0;

    // --- ESPECIFICACIONES TÉCNICAS DEL MODELO ---
    private String marca;
    private String modelo;
    private String almacenamiento;
    private String ram;
    private String color;
    
    /**
     * Condición comercial general: 'Nuevo / Sellado', 'Open Box', 'Seminuevo'
     */
    private String estado;

    @Lob
    @Column(name = "imagen_url", columnDefinition = "LONGTEXT")
    private String imagenUrl;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /**
     * Unidades físicas asociadas a este modelo.
     */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ItemCelular> itemsCelulares = new ArrayList<>();

    /**
     * Cálculo dinámico de stock disponible en tiempo de ejecución para Celulares.
     */
    @Formula("(SELECT COUNT(*) FROM item_celular ic WHERE ic.producto_id = id AND ic.estado_dispositivo = 'Disponible')")
    private Integer stockDisponibleCalculado;

    /**
     * Retorna el stock efectivo según la categoría:
     * - Si es 'Celulares': Retorna el conteo de unidades hijas en estado 'Disponible'.
     * - Si es Accesorio: Retorna el campo manual 'stock'.
     */
    @Transient
    public int getStockEfectivo() {
        if (categoria != null && "Celulares".equalsIgnoreCase(categoria.getNombre())) {
            return stockDisponibleCalculado != null ? stockDisponibleCalculado : 0;
        }
        return stock;
    }

    // Helper methods para sincronizar la relación bidireccional
    public void addItemCelular(ItemCelular item) {
        itemsCelulares.add(item);
        item.setProducto(this);
    }

    public void removeItemCelular(ItemCelular item) {
        itemsCelulares.remove(item);
        item.setProducto(null);
    }
}
