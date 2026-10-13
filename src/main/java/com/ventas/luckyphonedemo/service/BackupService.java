package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.Garantia;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.CategoriaRepository;
import com.ventas.luckyphonedemo.repositorio.GarantiaRepository;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ItemCelularRepository itemCelularRepository;
    private final GarantiaRepository garantiaRepository;

    private String escapeSql(String str) {
        if (str == null) return "NULL";
        return "'" + str.replace("'", "''").replace("\\", "\\\\") + "'";
    }

    @Transactional(readOnly = true)
    public byte[] generarSqlDump() {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("-- ==========================================================\n");
        sb.append("-- LUCKYPHONE BACKUP NATIVO DE BASE DE DATOS (MYSQL)\n");
        sb.append("-- FECHA GENERACIÓN: ").append(timestamp).append("\n");
        sb.append("-- COMPATIBILIDAD: MySQL 8.0+ / 9.x+ (InnoDB / utf8mb4)\n");
        sb.append("-- ==========================================================\n\n");
        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n");
        sb.append("SET NAMES utf8mb4;\n\n");

        // 1. Categorías
        List<Categoria> categorias = categoriaRepository.findAll();
        sb.append("-- --------------------------------------------------------\n");
        sb.append("-- TABLA: categoria\n");
        sb.append("-- --------------------------------------------------------\n");
        for (Categoria c : categorias) {
            sb.append(String.format("INSERT INTO categoria (id, nombre) VALUES (%d, %s) ON DUPLICATE KEY UPDATE nombre=%s;\n",
                    c.getId(), escapeSql(c.getNombre()), escapeSql(c.getNombre())));
        }
        sb.append("\n");

        // 2. Productos
        List<Producto> productos = productoRepository.findAll();
        sb.append("-- --------------------------------------------------------\n");
        sb.append("-- TABLA: producto\n");
        sb.append("-- --------------------------------------------------------\n");
        for (Producto p : productos) {
            Long catId = p.getCategoria() != null ? p.getCategoria().getId() : 1L;
            sb.append(String.format("INSERT INTO producto (id, nombre, descripcion, precio, stock, marca, modelo, almacenamiento, ram, color, estado, activo, categoria_id) " +
                            "VALUES (%d, %s, %s, %.2f, %d, %s, %s, %s, %s, %s, %s, %s, %d) " +
                            "ON DUPLICATE KEY UPDATE precio=%.2f, stock=%d, activo=%s;\n",
                    p.getId(), escapeSql(p.getNombre()), escapeSql(p.getDescripcion()),
                    p.getPrecio(), p.getStock(), escapeSql(p.getMarca()), escapeSql(p.getModelo()),
                    escapeSql(p.getAlmacenamiento()), escapeSql(p.getRam()), escapeSql(p.getColor()),
                    escapeSql(p.getEstado()), p.getActivo() ? "TRUE" : "FALSE", catId,
                    p.getPrecio(), p.getStock(), p.getActivo() ? "TRUE" : "FALSE"));
        }
        sb.append("\n");

        // 3. Items Celulares (Hardware físico)
        List<ItemCelular> items = itemCelularRepository.findAll();
        sb.append("-- --------------------------------------------------------\n");
        sb.append("-- TABLA: item_celular\n");
        sb.append("-- --------------------------------------------------------\n");
        for (ItemCelular it : items) {
            Long prodId = it.getProducto() != null ? it.getProducto().getId() : null;
            if (prodId == null) continue;

            String fechaVentaStr = it.getFechaVenta() != null ? "'" + it.getFechaVenta().toString() + "'" : "NULL";

            sb.append(String.format("INSERT INTO item_celular (id, producto_id, imei_1, imei_2, numero_serial, bateria, estado_dispositivo, fecha_venta) " +
                            "VALUES (%d, %d, %s, %s, %s, %s, %s, %s) " +
                            "ON DUPLICATE KEY UPDATE estado_dispositivo=%s, fecha_venta=%s;\n",
                    it.getId(), prodId, escapeSql(it.getImei1()), escapeSql(it.getImei2()),
                    escapeSql(it.getNumeroSerial()), escapeSql(it.getBateria()),
                    escapeSql(it.getEstadoDispositivo() != null ? it.getEstadoDispositivo().name() : "Disponible"),
                    fechaVentaStr,
                    escapeSql(it.getEstadoDispositivo() != null ? it.getEstadoDispositivo().name() : "Disponible"),
                    fechaVentaStr));
        }
        sb.append("\n");

        // 4. Garantías / Proformas
        List<Garantia> garantias = garantiaRepository.findAll();
        sb.append("-- --------------------------------------------------------\n");
        sb.append("-- TABLA: garantias\n");
        sb.append("-- --------------------------------------------------------\n");
        for (Garantia g : garantias) {
            String fechaElimStr = g.getEliminadoAt() != null ? "'" + g.getEliminadoAt().toString() + "'" : "NULL";
            sb.append(String.format("INSERT INTO garantias (id_garantia, cliente_nombre, cliente_dni, cliente_telefono, categoria, producto_modelo, imei1, imei2, numero_serie, precio_base, descuento_monto, precio_final, fecha_emision, fecha_caducidad, duracion_garantia, emisor_nombre, estado_pago, eliminado_at) " +
                            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %.2f, %.2f, %.2f, '%s', '%s', %s, %s, %s, %s) " +
                            "ON DUPLICATE KEY UPDATE estado_pago=%s, eliminado_at=%s;\n",
                    escapeSql(g.getId()), escapeSql(g.getClienteNombre()), escapeSql(g.getClienteDni()),
                    escapeSql(g.getClienteTelefono()), escapeSql(g.getCategoria()), escapeSql(g.getProductoModelo()),
                    escapeSql(g.getImei1()), escapeSql(g.getImei2()), escapeSql(g.getNumeroSerie()),
                    g.getPrecioBase(), g.getDescuentoMonto() != null ? g.getDescuentoMonto() : 0.0, g.getPrecioFinal(),
                    g.getFechaEmision() != null ? g.getFechaEmision().toString() : LocalDateTime.now().toString(),
                    g.getFechaCaducidad() != null ? g.getFechaCaducidad().toString() : LocalDateTime.now().plusMonths(12).toString(),
                    escapeSql(g.getDuracionGarantia()), escapeSql(g.getEmisorNombre()),
                    escapeSql(g.getEstadoPago() != null ? g.getEstadoPago().name() : "PENDIENTE"),
                    fechaElimStr,
                    escapeSql(g.getEstadoPago() != null ? g.getEstadoPago().name() : "PENDIENTE"),
                    fechaElimStr));
        }
        sb.append("\n");

        sb.append("SET FOREIGN_KEY_CHECKS = 1;\n");
        sb.append("-- FIN DEL BACKUP LUCKYPHONE\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}