package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.model.Garantia;
import com.ventas.luckyphonedemo.repositorio.GarantiaRepository;
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

    public GarantiaController(GarantiaRepository garantiaRepository) {
        this.garantiaRepository = garantiaRepository;
    }

    /**
     * GET /api/garantias?anulados=false|true&estado=todos|vigentes|caducados&query=xyz
     * Retorna el listado de garantias con los dias restantes calculados y los conteos para las burbujas numÃ©ricas.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarGarantias(
            @RequestParam(value = "anulados", defaultValue = "false") boolean anulados,
            @RequestParam(value = "filtro", defaultValue = "todos") String filtro,
            @RequestParam(value = "query", defaultValue = "") String query) {

        List<Garantia> todas = garantiaRepository.findAll();
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Conteos globales para las burbujas dinamicas
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

        // BÃºsqueda en vivo por Cliente, DNI, ID o IMEI/Serie
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
            dto.put("activacion", g.getFechaEmision() != null ? g.getFechaEmision().format(formatter) : "");
            dto.put("caducidad", g.getFechaCaducidad() != null ? g.getFechaCaducidad().format(formatter) : "");
            dto.put("diasRestantes", diasRestantes);
            dto.put("estado", diasRestantes > 0 ? "Vigente" : "Caducado");
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
     * Genera automÃ¡ticamente el cÃ³digo correlativo de control (ej. G-105).
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
     * Guardar proforma/garantÃ­a emitida fÃ­sicamente en MySQL antes de imprimir PDF.
     */
    @PostMapping
    public ResponseEntity<Garantia> crearGarantia(@RequestBody Garantia garantia) {
        if (garantia.getFechaEmision() == null) {
            garantia.setFechaEmision(LocalDateTime.now());
        }
        if (garantia.getId() == null || garantia.getId().trim().isEmpty()) {
            long total = garantiaRepository.count();
            garantia.setId(String.format("G-%03d", total + 1));
        }
        Garantia guardada = garantiaRepository.save(garantia);
        return ResponseEntity.ok(guardada);
    }

    /**
     * DELETE /api/garantias/{id}
     * Soft Delete (Anular)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> anularGarantia(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        return garantiaRepository.findById(id).map(g -> {
            g.setEliminadoAt(LocalDateTime.now());
            garantiaRepository.save(g);
            response.put("success", true);
            response.put("message", "GarantÃ­a anulada correctamente.");
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("success", false);
            response.put("message", "GarantÃ­a no encontrada.");
            return ResponseEntity.status(404).body(response);
        });
    }

    /**
     * POST /api/garantias/{id}/restaurar
     * Restaurar Soft Delete (Papelera de reciclaje)
     */
    @PostMapping("/{id}/restaurar")
    public ResponseEntity<Map<String, Object>> restaurarGarantia(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        return garantiaRepository.findById(id).map(g -> {
            g.setEliminadoAt(null);
            garantiaRepository.save(g);
            response.put("success", true);
            response.put("message", "GarantÃ­a restaurada correctamente.");
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("success", false);
            response.put("message", "GarantÃ­a no encontrada.");
            return ResponseEntity.status(404).body(response);
        });
    }
}