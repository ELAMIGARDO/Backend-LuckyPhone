package com.ventas.luckyphonedemo.repositorio;

import com.ventas.luckyphonedemo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<Cliente> findByEmail(String email); // Útil para el futuro Login
}
