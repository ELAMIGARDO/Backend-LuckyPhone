package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.dto.MetricsCountsDTO;
import com.ventas.luckyphonedemo.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metrics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final MetricsService metricsService;

    @GetMapping("/counts")
    public ResponseEntity<MetricsCountsDTO> obtenerConteos() {
        return ResponseEntity.ok(metricsService.obtenerConteosGlobales());
    }
}