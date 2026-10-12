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
     * Emitir proforma en estado PENDIENTE y reservar el ItemCelular si aplica.
     */
    @Transactional
    public Garantia emitirProforma(Garantia garantia) {
        if (garantia.getFechaEmision() == null) {
            garantia.setFechaEmision(LocalDateTime.now());
        }
        if (garantia.getId() == null || garantia.getId().trim().isEmpty()) {
            long total = garantiaRepository.count();
            garantia.setId(String.format("G-%03d", total + 1));
        }
        garantia.setEstadoPago(EstadoPago.PENDIENTE);

        // Si se indicó un IMEI y es Celular, validar disponibilidad y pasar a RESERVADO
        if (garantia.getImei1() != null && !garantia.getImei1().trim().isEmpty()) {
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(garantia.getImei1().trim());
            if (itemOpt.isPresent()) {
                ItemCelular item = itemOpt.get();
                if (item.getEstadoDispositivo() == EstadoDispositivo.Vendido) {
                    throw new BadRequestException("El dispositivo con IMEI " + garantia.getImei1() + " ya ha sido vendido.");
                }
                item.setEstadoDispositivo(EstadoDispositivo.Reservado);
                itemCelularRepository.save(item);
            }
        }

        return garantiaRepository.save(garantia);
    }

    /**
     * Registrar Pago (PAGADO): Pasa a Vendido el ItemCelular y activa la vigencia de garantía.
     */
    @Transactional
    public Garantia registrarPago(String id) {
        Garantia garantia = garantiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma no encontrada con ID: " + id));

        if (garantia.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BadRequestException("La proforma ya se encuentra registrada como PAGADA.");
        }
        if (garantia.getEstadoPago() == EstadoPago.ANULADO) {
            throw new BadRequestException("No se puede pagar una proforma que ha sido ANULADA.");
        }

        LocalDateTime fechaPago = LocalDateTime.now();
        garantia.setEstadoPago(EstadoPago.PAGADO);
        garantia.setFechaEmision(fechaPago);

        // Recalcular caducidad según la duración especificada
        if (garantia.getDuracionGarantia() != null) {
            String dur = garantia.getDuracionGarantia().toLowerCase();
            if (dur.contains("12") || dur.contains("1 año") || dur.contains("1 ano")) {
                garantia.setFechaCaducidad(fechaPago.plusMonths(12));
            } else if (dur.contains("6")) {
                garantia.setFechaCaducidad(fechaPago.plusMonths(6));
            } else if (dur.contains("3")) {
                garantia.setFechaCaducidad(fechaPago.plusMonths(3));
            } else if (dur.contains("1 mes")) {
                garantia.setFechaCaducidad(fechaPago.plusMonths(1));
            } else {
                garantia.setFechaCaducidad(fechaPago.plusMonths(6));
            }
        }

        // Marcar el hardware físico como Vendido
        if (garantia.getImei1() != null && !garantia.getImei1().trim().isEmpty()) {
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(garantia.getImei1().trim());
            if (itemOpt.isPresent()) {
                ItemCelular item = itemOpt.get();
                item.setEstadoDispositivo(EstadoDispositivo.Vendido);
                itemCelularRepository.save(item);
            }
        }

        return garantiaRepository.save(garantia);
    }

    /**
     * Anular proforma: Libera el ItemCelular de nuevo a DISPONIBLE y cancela la garantía.
     */
    @Transactional
    public Garantia anularProforma(String id) {
        Garantia garantia = garantiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma no encontrada con ID: " + id));

        garantia.setEstadoPago(EstadoPago.ANULADO);
        garantia.setEliminadoAt(LocalDateTime.now());

        // Liberar hardware
        if (garantia.getImei1() != null && !garantia.getImei1().trim().isEmpty()) {
            Optional<ItemCelular> itemOpt = itemCelularRepository.findByImei1(garantia.getImei1().trim());
            if (itemOpt.isPresent()) {
                ItemCelular item = itemOpt.get();
                item.setEstadoDispositivo(EstadoDispositivo.Disponible);
                itemCelularRepository.save(item);
            }
        }

        return garantiaRepository.save(garantia);
    }
}
