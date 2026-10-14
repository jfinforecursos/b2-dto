package com.example.apibasica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apibasica.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Método personalizado para buscar pedidos asociados a un cliente específico
    List<Pedido> findByClienteId(Long clienteId);
}
