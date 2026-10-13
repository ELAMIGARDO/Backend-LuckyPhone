package com.ventas.luckyphonedemo.config;

import com.ventas.luckyphonedemo.model.Categoria;
import com.ventas.luckyphonedemo.model.Cliente;
import com.ventas.luckyphonedemo.model.Producto;
import com.ventas.luckyphonedemo.repositorio.CategoriaRepository;
import com.ventas.luckyphonedemo.repositorio.ClienteRepository;
import com.ventas.luckyphonedemo.repositorio.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Inicializar Categorias predeterminadas
        List<String> categorias = List.of("Celulares", "Cargadores", "Audífonos", "Accesorios");
        for (String nombreCat : categorias) {
            if (!categoriaRepository.existsByNombre(nombreCat)) {
                Categoria cat = new Categoria();
                cat.setNombre(nombreCat);
                categoriaRepository.save(cat);
            }
        }

        // 2. Inicializar Usuario ADMIN (123456@)
        if (!clienteRepository.existsByEmail("admin@luckyphone.com")) {
            Cliente admin = new Cliente();
            admin.setNombre("Admin");
            admin.setApellido("Principal");
            admin.setDni("11111111");
            admin.setTelefono("999999999");
            admin.setEmail("admin@luckyphone.com");
            admin.setDireccion("Av. Central 123");
            admin.setPassword(passwordEncoder.encode("123456@"));
            admin.setRol("ROLE_ADMIN");
            clienteRepository.save(admin);
        } else {
            Cliente admin = clienteRepository.findByEmail("admin@luckyphone.com").orElse(null);
            if (admin != null && !admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode("123456@"));
                admin.setRol("ROLE_ADMIN");
                clienteRepository.save(admin);
            }
        }

        // 3. Inicializar Usuario USER (Egusquiza2546)
        if (!clienteRepository.existsByEmail("adrian2546@gmail.com")) {
            Cliente user = new Cliente();
            user.setNombre("Adrian");
            user.setApellido("Egusquiza");
            user.setDni("22222222");
            user.setTelefono("988888888");
            user.setEmail("adrian2546@gmail.com");
            user.setDireccion("Calle Los Pinos 456");
            user.setPassword(passwordEncoder.encode("Egusquiza2546"));
            user.setRol("ROLE_USER");
            clienteRepository.save(user);
        } else {
            Cliente user = clienteRepository.findByEmail("adrian2546@gmail.com").orElse(null);
            if (user != null && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode("Egusquiza2546"));
                user.setRol("ROLE_USER");
                clienteRepository.save(user);
            }
        }

        // 4. Auto-curacion de stock en BD MySQL para sincronizar unidades fisicas
        try {
            List<Producto> productos = productoRepository.findAll();
            for (Producto p : productos) {
                int stockReal = p.getStockEfectivo();
                if (p.getStock() != stockReal) {
                    p.setStock(stockReal);
                    productoRepository.save(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Advertencia al sincronizar stock inicial: " + e.getMessage());
        }
    }
}