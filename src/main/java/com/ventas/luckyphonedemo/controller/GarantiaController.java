package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.model.EstadoPago;
import com.ventas.luckyphonedemo.model.Garantia;
import com.ventas.luckyphonedemo.repositorio.GarantiaRepository;
import com.ventas.luckyphonedemo.service.ProformaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/garantias")
@CrossOrigin(origins = "*")
public class GarantiaController {

    private final GarantiaRepository garantiaRepository;
    private final ProformaService proformaService;

    public GarantiaController(GarantiaRepository garantiaRepository, ProformaService proformaService) {
        this.garantiaRepository = garantiaRepository;
        this.proformaService = proformaService;
    }

    /**
     * GET /api/garantias?anulados=false|true&filtro=todos|vigentes|caducados&query=xyz
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarGarantias(
            @RequestParam(value = "anulados", defaultValue = "false") boolean anulados,
            @RequestParam(value = "filtro", defaultValue = "todos") String filtro,
            @RequestParam(value = "query", defaultValue = "") String query) {

        List<Garantia> todas = garantiaRepository.findAll();
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Conteos globales para las burbujas dinámicas
        long countTotales = todas.stream().filter(g -> g.getEliminadoAt() == null).count();
        long countVigentes = todas.stream().filter(g -> g.getEliminadoAt() == null && g.getFechaCaducidad().isAfter(ahora)).count();
        long countCaducados = todas.stream().filter(g -> g.getEliminadoAt() == null && !g.getFechaCaducidad().isAfter(ahora)).count();
        long countAnulados = todas.stream().filter(g -> g.getEliminadoAt() != null).count();

        Map<String, Long> conteos = new HashMap<>();
        conteos.put("totales", countTotales);
        conteos.put("vigentes", countVigentes);
        conteos.put("caducados", countCaducados);
        conteos.put("anulados", countAnulados);

        // Filtrado por Soft Delete
        List<Garantia> listaFiltrada = todas.stream()
                .filter(g -> anulados ? g.getEliminadoAt() != null : g.getEliminadoAt() == null)
                .collect(Collectors.toList());

        // Filtrado adicional por vigencia si aplica
        if (!anulados) {
            if ("vigentes".equalsIgnoreCase(filtro)) {
                listaFiltrada = listaFiltrada.stream().filter(g -> g.getFechaCaducidad().isAfter(ahora)).collect(Collectors.toList());
            } else if ("caducados".equalsIgnoreCase(filtro)) {
                listaFiltrada = listaFiltrada.stream().filter(g -> !g.getFechaCaducidad().isAfter(ahora)).collect(Collectors.toList());
            }
        }

        // Búsqueda en vivo por Cliente, DNI, ID o IMEI/Serie
        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase().trim();
            listaFiltrada = listaFiltrada.stream().filter(g ->
                (g.getId() != null && g.getId().toLowerCase().contains(q)) ||
                (g.getClienteNombre() != null && g.getClienteNombre().toLowerCase().contains(q)) ||
                (g.getClienteDni() != null && g.getClienteDni().toLowerCase().contains(q)) ||
                (g.getProductoModelo() != null && g.getProductoModelo().toLowerCase().contains(q)) ||
                (g.getImei1() != null && g.getImei1().toLowerCase().contains(q)) ||
                (g.getNumeroSerie() != null && g.getNumeroSerie().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }

        // Mapear a DTO con diasRestantes calculados
        List<Map<String, Object>> dataDto = new ArrayList<>();
        for (Garantia g : listaFiltrada) {
            Map<String, Object> dto = new HashMap<>();
            long diasRestantes = Duration.between(ahora, g.getFechaCaducidad()).toDays();

            dto.put("id", g.getId());
            dto.put("cliente", g.getClienteNombre());
            dto.put("dni", g.getClienteDni());
            dto.put("telefono", g.getClienteTelefono());
            dto.put("celular", g.getProductoModelo());
            dto.put("categoria", g.getCategoria());
            dto.put("imei1", g.getImei1());
            dto.put("imei2", g.getImei2());
            dto.put("numeroSerie", g.getNumeroSerie());
            dto.put("precioBase", g.getPrecioBase());
            dto.put("descuentoMonto", g.getDescuentoMonto());
            dto.put("precioFinal", g.getPrecioFinal());
            dto.put("activacion", g.getFechaEmision() != null ? g.getFechaEmision().format(formatter) : "");
            dto.put("caducidad", g.getFechaCaducidad() != null ? g.getFechaCaducidad().format(formatter) : "");
            dto.put("duracionGarantia", g.getDuracionGarantia());
            dto.put("diasRestantes", diasRestantes);
            dto.put("estado", diasRestantes > 0 ? "Vigente" : "Caducado");
            dto.put("estadoPago", g.getEstadoPago() != null ? g.getEstadoPago().name() : "PENDIENTE");
            dto.put("alerta", diasRestantes <= 0);
            dto.put("eliminadoAt", g.getEliminadoAt());

            dataDto.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", dataDto);
        response.put("conteos", conteos);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/garantias/siguiente-codigo
     */
    @GetMapping("/siguiente-codigo")
    public ResponseEntity<Map<String, String>> obtenerSiguienteCodigo() {
        long total = garantiaRepository.count();
        String siguienteId = String.format("G-%03d", total + 1);
        Map<String, String> res = new HashMap<>();
        res.put("codigo", siguienteId);
        return ResponseEntity.ok(res);
    }

    /**
     * POST /api/garantias
     * Emitir proforma (estado PENDIENTE, reserva hardware si es celular).
     */
    @PostMapping
    public ResponseEntity<Garantia> crearGarantia(@RequestBody Garantia garantia) {
        Garantia emitida = proformaService.emitirProforma(garantia);
        return ResponseEntity.ok(emitida);
    }

    /**
     * PUT /api/garantias/{id}
     * Editar póliza/datos de garantía existente (sin 403 Forbidden).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Garantia> actualizarGarantia(@PathVariable String id, @RequestBody Garantia request) {
        return garantiaRepository.findById(id).map(g -> {
            if (request.getClienteNombre() != null) g.setClienteNombre(request.getClienteNombre());
            if (request.getClienteDni() != null) g.setClienteDni(request.getClienteDni());
            if (request.getClienteTelefono() != null) g.setClienteTelefono(request.getClienteTelefono());
            if (request.getProductoModelo() != null) g.setProductoModelo(request.getProductoModelo());
            if (request.getCategoria() != null) g.setCategoria(request.getCategoria());
            if (request.getImei1() != null) g.setImei1(request.getImei1());
            if (request.getImei2() != null) g.setImei2(request.getImei2());
            if (request.getNumeroSerie() != null) g.setNumeroSerie(request.getNumeroSerie());
            if (request.getDuracionGarantia() != null) g.setDuracionGarantia(request.getDuracionGarantia());
            if (request.getPrecioBase() != null) g.setPrecioBase(request.getPrecioBase());
            if (request.getDescuentoMonto() != null) g.setDescuentoMonto(request.getDescuentoMonto());
            if (request.getPrecioFinal() != null) g.setPrecioFinal(request.getPrecioFinal());
            if (request.getEmisorNombre() != null) g.setEmisorNombre(request.getEmisorNombre());
            if (request.getFechaCaducidad() != null) g.setFechaCaducidad(request.getFechaCaducidad());

            Garantia guardada = garantiaRepository.save(g);
            return ResponseEntity.ok(guardada);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/garantias/{id}/pagar
     * Transición PENDIENTE -> PAGADO (Hardware marcado como Vendido, garantía activada).
     */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<Map<String, Object>> registrarPago(@PathVariable String id) {
        Garantia pagada = proformaService.registrarPago(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Pago registrado con éxito y hardware marcado como Vendido.");
        response.put("garantia", pagada);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/garantias/{id}
     * Transición a ANULADO (Hardware liberado a Disponible, garantía anulada).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> anularGarantia(@PathVariable String id) {
        Garantia anulada = proformaService.anularProforma(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Proforma anulada y hardware liberado.");
        response.put("garantia", anulada);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/garantias/{id}/restaurar
     * Restaurar Garantía si estaba anulada
     */
    @PostMapping("/{id}/restaurar")
    public ResponseEntity<Map<String, Object>> restaurarGarantia(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        return garantiaRepository.findById(id).map(g -> {
            g.setEliminadoAt(null);
            g.setEstadoPago(EstadoPago.PENDIENTE);
            garantiaRepository.save(g);
            response.put("success", true);
            response.put("message", "Garantía restaurada correctamente.");
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("success", false);
            response.put("message", "Garantía no encontrada.");
            return ResponseEntity.status(404).body(response);
        });
    }
}
