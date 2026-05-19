package com.ventas.luckyphonedemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja tus excepciones personalizadas (ResourceNotFound y BadRequest)
    @ExceptionHandler({ResourceNotFoundException.class, BadRequestException.class})
    public ResponseEntity<Map<String, String>> manejarCustomExceptions(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());

        // Si es ResourceNotFound mandamos 404, si no 400
        HttpStatus status = ex instanceof ResourceNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(error, status);
    }

    // Este maneja los errores de validación de los @NotBlank, @Email, @Size de tu entidad
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}