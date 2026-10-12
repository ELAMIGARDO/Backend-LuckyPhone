package com.ventas.luckyphonedemo.service;

import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.model.EstadoDispositivo;
import com.ventas.luckyphonedemo.model.EstadoPago;
import com.ventas.luckyphonedemo.model.Garantia;
import com.ventas.luckyphonedemo.model.ItemCelular;
import com.ventas.luckyphonedemo.repositorio.GarantiaRepository;
import com.ventas.luckyphonedemo.repositorio.ItemCelularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProformaService {

    @Autowired
    private GarantiaRepository garantiaRepository;

    @Autowired
    private ItemCelularRepository itemCelularRepository;

    /**
     * Emitir proforma en estado PENDIENTE y validar que el celular/IMEI exista y esté disponible.
     */
    @Transactional(rollbackFor = Exception.class)
    public Garantia emitirProforma(Garantia garantia) {
        if (garantia.getFechaEmision() == null) {
            garantia.setFechaEmision(LocalDateTime.now());
        }
        if (garantia.getId() == null || garantia.getId().trim().isEmpty()) {
            long total = garantiaRepository.count();
            garantia.setId(String.format("G-%03d", total + 1));
        }
        garantia.setEstadoPago(EstadoPago.PENDIENTE);

        // Si la categoría es Celulares, validar existencia y disponibilidad del IMEI
        boolean isCelular = garantia.getCategoria() != null && garantia.getCategoria().equalsIgnoreCase("Celulares");
        if (isCelular) {
            if (garantia.getImei1() == null || garantia.getImei1().trim().isEmpty()) {
                throw new BadRequestException("El IMEI 1 es obligatorio para emitir la proforma de un teléfono celular.");
            }

            String imeiTrim = garantia.getImei1().trim();
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(imeiTrim);
            if (itemOpt.isEmpty()) {
                throw new BadRequestException("No existe ningún celular registrado en el inventario con el IMEI: " + imeiTrim);
            }

            ItemCelular item = itemOpt.get();
            if (item.getEstadoDispositivo() == EstadoDispositivo.Vendido) {
                throw new BadRequestException("El dispositivo con IMEI " + imeiTrim + " ya ha sido vendido previamente.");
            }
            if (item.getEstadoDispositivo() == EstadoDispositivo.Reservado) {
                throw new BadRequestException("El dispositivo con IMEI " + imeiTrim + " ya se encuentra reservado en otra proforma activa.");
            }

            // Reservar el equipo para esta proforma
            item.setEstadoDispositivo(EstadoDispositivo.Reservado);
            item.setActualizadoAt(LocalDateTime.now());
            itemCelularRepository.save(item);
        } else if (garantia.getImei1() != null && !garantia.getImei1().trim().isEmpty()) {
            // Caso accesorio que opcionalmente tenga IMEI/Item
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(garantia.getImei1().trim());
            if (itemOpt.isPresent()) {
                ItemCelular item = itemOpt.get();
                if (item.getEstadoDispositivo() == EstadoDispositivo.Vendido) {
                    throw new BadRequestException("El dispositivo con IMEI " + garantia.getImei1() + " ya ha sido vendido.");
                }
                item.setEstadoDispositivo(EstadoDispositivo.Reservado);
                item.setActualizadoAt(LocalDateTime.now());
                itemCelularRepository.save(item);
            }
        }

        return garantiaRepository.save(garantia);
    }

    /**
     * Cobro de Proforma y Activación de Garantía con bloqueo de hardware físico.
     * En caso de cualquier inconsistencia o fallo, Spring ejecuta Rollback total inmediato.
     */
    @Transactional(rollbackFor = Exception.class)
    public Garantia registrarPago(String id) {
        return registrarPagoProforma(id);
    }

    /**
     * Alias del método transaccional para cobro y emisión de venta.
     */
    @Transactional(rollbackFor = Exception.class)
    public Garantia registrarPagoProforma(String proformaId) {
        // 1. Validar la existencia de la proforma y verificar estado PENDIENTE
        Garantia proforma = garantiaRepository.findById(proformaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la proforma con código: " + proformaId));

        if (proforma.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BadRequestException("La proforma " + proformaId + " ya fue cobrada y liquidada previamente.");
        }

        if (proforma.getEstadoPago() == EstadoPago.ANULADO) {
            throw new BadRequestException("Operación inválida: La proforma " + proformaId + " se encuentra ANULADA.");
        }

        // 2. Si corresponde a un Celular con IMEI, validar disponibilidad física y actualizar a Vendido
        if (proforma.getImei1() != null && !proforma.getImei1().trim().isEmpty()) {
            String imei = proforma.getImei1().trim();
            ItemCelular item = itemCelularRepository.findByImei1(imei)
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no hallado en inventario con IMEI: " + imei));

            // Solo se permite la venta si está Disponible o Reservado para esta misma proforma
            if (item.getEstadoDispositivo() == EstadoDispositivo.Vendido) {
                throw new BadRequestException("Conflicto de concurrencia: El dispositivo con IMEI " + imei 
                        + " ya fue marcado como VENDIDO en otra transacción.");
            }

            // 3. Pasar hardware a 'Vendido' y registrar actualización
            item.setEstadoDispositivo(EstadoDispositivo.Vendido);
            item.setActualizadoAt(LocalDateTime.now());
            itemCelularRepository.save(item);
        }

        // 4. Activar póliza de garantía en tiempo real (PAGADO)
        LocalDateTime ahora = LocalDateTime.now();
        proforma.setEstadoPago(EstadoPago.PAGADO);
        proforma.setFechaEmision(ahora);

        // Calcular fecha de caducidad según los términos acordados
        int mesesGarantia = 12; // Valor por defecto
        if (proforma.getDuracionGarantia() != null) {
            String dur = proforma.getDuracionGarantia().toLowerCase();
            if (dur.contains("1 mes")) mesesGarantia = 1;
            else if (dur.contains("3")) mesesGarantia = 3;
            else if (dur.contains("6")) mesesGarantia = 6;
            else if (dur.contains("12") || dur.contains("1 año") || dur.contains("1 ano")) mesesGarantia = 12;
            else {
                try {
                    mesesGarantia = Integer.parseInt(dur.replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        proforma.setFechaCaducidad(ahora.plusMonths(mesesGarantia));

        // 5. Persistir y confirmar transacción de forma atómica
        return garantiaRepository.save(proforma);
    }

    /**
     * Anular proforma: Libera el ItemCelular de nuevo a DISPONIBLE y cancela la garantía.
     */
    @Transactional(rollbackFor = Exception.class)
    public Garantia anularProforma(String id) {
        Garantia garantia = garantiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma no encontrada con ID: " + id));

        garantia.setEliminadoAt(LocalDateTime.now());
        garantia.setEstadoPago(EstadoPago.ANULADO);

        // Si tenía hardware reservado, devolverlo a Disponible
        if (garantia.getImei1() != null && !garantia.getImei1().trim().isEmpty()) {
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(garantia.getImei1().trim());
            if (itemOpt.isPresent()) {
                ItemCelular item = itemOpt.get();
                item.setEstadoDispositivo(EstadoDispositivo.Disponible);
                item.setActualizadoAt(LocalDateTime.now());
                itemCelularRepository.save(item);
            }
        }

        return garantiaRepository.save(garantia);
    }
}
