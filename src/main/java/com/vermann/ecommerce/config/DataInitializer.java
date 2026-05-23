package com.vermann.ecommerce.config;

import com.vermann.ecommerce.model.*;
import com.vermann.ecommerce.repository.ProductRepository;
import com.vermann.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        String hash = passwordEncoder.encode("password123");

        List<User> users = List.of(
            User.builder().name("Admin Sistema").email("admin@ecommerce.com")
                .passwordHash(hash).role(UserRole.ADMIN).active(true).build(),
            User.builder().name("María González").email("maria@example.com")
                .passwordHash(hash).role(UserRole.CUSTOMER).active(true).build(),
            User.builder().name("Carlos Pérez").email("carlos@example.com")
                .passwordHash(hash).role(UserRole.CUSTOMER).active(true).build()
        );

        userRepository.saveAll(users);
        log.info("Seed: {} users created", users.size());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
            Product.builder().name("Laptop Pro 15").price(new BigDecimal("1299.99"))
                .stock(10).category("ELECTRONICS").active(true).build(),
            Product.builder().name("Mouse Inalámbrico").price(new BigDecimal("29.99"))
                .stock(100).category("ELECTRONICS").active(true).build(),
            Product.builder().name("Camiseta Básica").price(new BigDecimal("19.99"))
                .stock(200).category("CLOTHING").active(true).build(),
            Product.builder().name("Zapatillas Running").price(new BigDecimal("89.99"))
                .stock(50).category("CLOTHING").active(true).build(),
            Product.builder().name("Libro Spring Boot").price(new BigDecimal("45.00"))
                .stock(30).category("BOOKS").active(true).build(),
            Product.builder().name("Producto Inactivo").price(new BigDecimal("9.99"))
                .stock(0).category("OTHER").active(false).build()
        );

        productRepository.saveAll(products);
        log.info("Seed: {} products created", products.size());
    }
}
