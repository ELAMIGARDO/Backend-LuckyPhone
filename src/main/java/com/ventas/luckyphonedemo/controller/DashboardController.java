package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.dto.DashboardMetricsDTO;
import com.ventas.luckyphonedemo.dto.MetricsCountsDTO;
import com.ventas.luckyphonedemo.service.DashboardService;
import com.ventas.luckyphonedemo.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/dashboard", "/api/admin/metrics"})
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final MetricsService metricsService;

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> obtenerMetricasDashboard() {
        return ResponseEntity.ok(dashboardService.obtenerMetricas());
    }

    @GetMapping("/counts")
    public ResponseEntity<MetricsCountsDTO> obtenerConteoBadges() {
        return ResponseEntity.ok(metricsService.obtenerConteosGlobales());
    }
}