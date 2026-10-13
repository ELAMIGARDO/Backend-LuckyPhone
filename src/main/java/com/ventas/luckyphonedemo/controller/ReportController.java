package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/reports", "/api/reportes"})
@RequiredArgsConstructor
public class ReportController {

    private final ReporteService reporteService;

    @GetMapping({"/export/excel", "/excel"})
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(defaultValue = "TODOS") String categoria,
            @RequestParam(defaultValue = "todos") String periodo) {

        try {
            byte[] contenido = reporteService.generarExcelInventario(categoria, periodo);
            String nombreArchivo = "luckyphone_rotacion_" + LocalDate.now() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(contenido);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel: " + e.getMessage(), e);
        }
    }

    @GetMapping({"/export/pdf", "/pdf"})
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(defaultValue = "TODOS") String categoria,
            @RequestParam(defaultValue = "todos") String periodo) {

        try {
            byte[] contenido = reporteService.generarPdfContable(categoria, periodo);
            String nombreArchivo = "luckyphone_auditoria_contable_" + LocalDate.now() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(contenido);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte PDF: " + e.getMessage(), e);
        }
    }
}