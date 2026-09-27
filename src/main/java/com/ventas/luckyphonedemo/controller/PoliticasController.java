package com.ventas.luckyphonedemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/politicas")
public class PoliticasController {

    @GetMapping("/terminos")
    public ResponseEntity<Map<String, Object>> obtenerTerminosYCondiciones() {
        Map<String, Object> response = new HashMap<>();
        response.put("titulo", "Términos y Condiciones de Uso - LuckyPhone");
        response.put("version", "1.0");
        response.put("fechaActualizacion", "2026-09-27");
        response.put("contenido", "El presente contrato regula el uso del sistema LuckyPhone. El sistema se proporciona 'Tal Cual' (As-Is). El desarrollador no asume responsabilidad por daños indirectos o pérdidas comerciales del usuario.");
        response.put("limitacionResponsabilidad", "La responsabilidad máxima del proveedor se limita al costo del servicio contratado.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/privacidad")
    public ResponseEntity<Map<String, Object>> obtenerPoliticaPrivacidad() {
        Map<String, Object> response = new HashMap<>();
        response.put("titulo", "Política de Privacidad y Protección de Datos Personales");
        response.put("version", "1.0");
        response.put("leyAplicable", "Ley de Protección de Datos Personales");
        response.put("datosRecolectados", new String[]{"Nombre", "Apellido", "DNI", "Teléfono", "Email", "Dirección"});
        response.put("finalidad", "Gestión de catálogo, autenticación segura y coordinación de compras comerciales mediante WhatsApp.");
        response.put("seguridad", "Las contraseñas se almacenan mediante algoritmos de cifrado de sentido único (BCrypt). Los accesos están protegidos por tokens JWT.");
        response.put("derechosARCO", "El usuario tiene derecho a solicitar la rectificación o eliminación de sus datos personales.");
        return ResponseEntity.ok(response);
    }
}
