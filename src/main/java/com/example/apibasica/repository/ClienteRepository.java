package com.example.apibasica.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apibasica.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Método personalizado para buscar un cliente por su correo electrónico
    Optional<Cliente> findByEmail(String email);
}
