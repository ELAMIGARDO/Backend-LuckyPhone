package com.ventas.luckyphonedemo.controller;

import com.ventas.luckyphonedemo.model.Cliente;
import com.ventas.luckyphonedemo.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor

public class ClienteController {
    private final ClienteService service;

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodos(){
        return ResponseEntity.ok(service.obtenerTodos());
    }
    @GetMapping("/{id}")
    public  ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id){
        return ResponseEntity.ok(service.obtenerClienteId(id));
    }
    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(cliente));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        return ResponseEntity.ok(service.actualizar(id, cliente));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
