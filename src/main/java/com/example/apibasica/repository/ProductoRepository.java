package com.example.apibasica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apibasica.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

