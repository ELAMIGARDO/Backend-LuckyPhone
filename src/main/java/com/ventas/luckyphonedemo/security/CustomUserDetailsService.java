package com.ventas.luckyphonedemo.security;

import com.ventas.luckyphonedemo.model.Cliente;
import com.ventas.luckyphonedemo.repositorio.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        String rol = cliente.getRol() != null ? cliente.getRol() : "ROLE_USER";
        if (!rol.startsWith("ROLE_")) {
            rol = "ROLE_" + rol;
        }

        return new User(
                cliente.getEmail(),
                cliente.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(rol))
        );
    }
}
