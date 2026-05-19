package com.ventas.luckyphonedemo.service;
import com.ventas.luckyphonedemo.exception.BadRequestException;
import com.ventas.luckyphonedemo.exception.ResourceNotFoundException;
import com.ventas.luckyphonedemo.model.Cliente;
import com.ventas.luckyphonedemo.repositorio.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    public List<Cliente> obtenerTodos(){
        return repository.findAll();
    }
    public Cliente obtenerClienteId(Long id){
        return repository.findById(id).orElseThrow(()-> new
                ResourceNotFoundException("Cliente no encontrado"));
    }
    public Cliente actualizar(Long id, Cliente clienteDetalles) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar, cliente no encontrado"));

        // Actualizamos los campos (con la entidad de Cliente)
        cliente.setNombre(clienteDetalles.getNombre());
        cliente.setApellido(clienteDetalles.getApellido());
        cliente.setDni(clienteDetalles.getDni());
        cliente.setTelefono(clienteDetalles.getTelefono());
        cliente.setEmail(clienteDetalles.getEmail());
        cliente.setDireccion(clienteDetalles.getDireccion());
        cliente.setPassword(clienteDetalles.getPassword());
        return repository.save(cliente);
    }

    public Cliente crear(Cliente cliente) {
        //validacion del email
       if (repository.existsByEmail(cliente.getEmail())){
           throw new BadRequestException("El Email ya esta registrado");
       }
        //validacion del dni
       if (repository.existsByDni(cliente.getDni())){
           throw new BadRequestException(("El DNI ya esta registrado"));
       }

        return repository.save(cliente);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, cliente no existe");
        }
        repository.deleteById(id);
    }
}


