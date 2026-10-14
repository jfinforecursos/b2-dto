package com.example.apibasica.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.apibasica.model.Cliente;
import com.example.apibasica.model.Pedido;
import com.example.apibasica.model.Producto;
import com.example.apibasica.repository.ClienteRepository;
import com.example.apibasica.repository.PedidoRepository;
import com.example.apibasica.repository.ProductoRepository;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner sembrarDatos(
            ClienteRepository clienteRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository) {
        return args -> {
            // Solo sembramos datos si la tabla de clientes está vacía para evitar duplicados
            if (clienteRepository.count() == 0) {

                // 1. Crear 2 clientes
                Cliente cliente1 = new Cliente();
                cliente1.setNombre("Juan Pérez");
                cliente1.setEmail("juan.perez@example.com");
                cliente1.setTelefono("555-1234");

                Cliente cliente2 = new Cliente();
                cliente2.setNombre("María Gómez");
                cliente2.setEmail("maria.gomez@example.com");
                cliente2.setTelefono("555-5678");

                // Guardamos los clientes primero para generar sus IDs en la base de datos
                clienteRepository.save(cliente1);
                clienteRepository.save(cliente2);

                // 2. Crear 5 pedidos asociados al Cliente 1
                Pedido p1 = new Pedido(null, "Laptop Lenovo ThinkPad", 850.0, cliente1);
                Pedido p2 = new Pedido(null, "Mouse inalámbrico Logitech", 25.5, cliente1);
                Pedido p3 = new Pedido(null, "Teclado mecánico Redragon", 65.0, cliente1);
                Pedido p4 = new Pedido(null, "Monitor 24 pulgadas LG", 180.0, cliente1);
                Pedido p5 = new Pedido(null, "Auriculares Bluetooth Sony", 45.0, cliente1);

                // 3. Crear 5 pedidos asociados al Cliente 2
                Pedido p6 = new Pedido(null, "Smartphone Samsung Galaxy", 600.0, cliente2);
                Pedido p7 = new Pedido(null, "Funda de cuero protectora", 15.0, cliente2);
                Pedido p8 = new Pedido(null, "Cargador rápido 45W", 30.0, cliente2);
                Pedido p9 = new Pedido(null, "Soporte metálico de escritorio", 20.0, cliente2);
                Pedido p10 = new Pedido(null, "Mochila para laptop", 50.0, cliente2);

                // Guardamos todos los pedidos
                pedidoRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));

                System.out.println(">>> SEED EXITOSA: Se han sembrado 2 clientes y 5 pedidos para cada uno.");
            }

            // Sembramos productos de ejemplo si la tabla de productos está vacía
            if (productoRepository.count() == 0) {
                // Notar que pasamos 'costoProveedor' (dato confidencial de compra), el cual NO se expondrá en los DTOs
                Producto prod1 = new Producto(null, "Teclado Mecánico RGB", "Switches mecánicos táctiles, retroiluminación configurable", 75.0, 30, 42.0);
                Producto prod2 = new Producto(null, "Mouse Ergonómico Inalámbrico", "Sensor óptico de alta precisión con batería recargable", 35.0, 50, 18.5);
                Producto prod3 = new Producto(null, "Monitor Gamer 27\" 144Hz", "Panel IPS resolución QHD 2K y tiempo de respuesta 1ms", 280.0, 15, 170.0);
                Producto prod4 = new Producto(null, "Auriculares con Cancelación de Ruido", "Conectividad Bluetooth 5.3 y micrófono integrado", 110.0, 20, 65.0);

                productoRepository.saveAll(List.of(prod1, prod2, prod3, prod4));
                System.out.println(">>> SEED PRODUCTOS EXITOSA: Se han sembrado 4 productos con costos de proveedor confidenciales.");
            }
        };
    }
}
