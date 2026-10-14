package com.example.apibasica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.apibasica.exception.RecursoNoEncontradoException;
import com.example.apibasica.exception.ReglaDeNegocioException;
import com.example.apibasica.model.Cliente;
import com.example.apibasica.repository.ClienteRepository;

@Service
public class ClienteService {

    // Inyección de dependencias por constructor manual (sin @Autowired ni Lombok)
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> obtenerTodos() {       
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));
    }

    public Cliente crear(Cliente cliente) {
        // Regla de negocio: no permitir el registro con un email ya existente
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe un cliente con el email: " + cliente.getEmail());
        }
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datosCliente) {
        Cliente cliente = obtenerPorId(id);

        // Regla de negocio: si cambia de email, verificar que el nuevo email no esté ocupado
        if (datosCliente.getEmail() != null && !datosCliente.getEmail().equalsIgnoreCase(cliente.getEmail())) {
            if (clienteRepository.findByEmail(datosCliente.getEmail()).isPresent()) {
                throw new ReglaDeNegocioException("El email " + datosCliente.getEmail() + " ya está en uso por otro cliente");
            }
        }

        cliente.setNombre(datosCliente.getNombre());
        cliente.setEmail(datosCliente.getEmail());
        cliente.setTelefono(datosCliente.getTelefono());

        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = obtenerPorId(id);
        clienteRepository.delete(cliente);
    }

    // Uso del método personalizado del repositorio
    public Cliente obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con email: " + email));
    }
}
