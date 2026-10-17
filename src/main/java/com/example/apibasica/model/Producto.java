package com.example.apibasica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {

    public Producto() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;

    // Dato sensible e interno de la empresa: NUNCA se debe exponer en los DTOs
    private Double costoProveedor;

    public Producto(Long id, String nombre, String descripcion, Double precio, Integer stock, Double costoProveedor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.costoProveedor = costoProveedor;
    }
}
