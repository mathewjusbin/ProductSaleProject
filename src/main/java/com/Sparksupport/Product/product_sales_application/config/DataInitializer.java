package com.sparksupport.product.product_sales_application.config;

import com.sparksupport.product.product_sales_application.model.Role;
import com.sparksupport.product.product_sales_application.model.User;
import com.sparksupport.product.product_sales_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123")); //how it encrypts ? what encrypton scheme
            adminUser.setRole(Role.ADMIN);
            adminUser.setEnabled(true);
            
            userRepository.save(adminUser);
            log.info("Default admin user created with username: 'admin' and password: 'admin123'");
        } else {
            log.info("Admin user already exists, skipping initialization");
        }

        // Optionally create a regular user as well
        if (!userRepository.existsByUsername("user")) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setPassword(passwordEncoder.encode("user123"));
            regularUser.setRole(Role.USER);
            regularUser.setEnabled(true);
            
            userRepository.save(regularUser);
            log.info("Default regular user created with username: 'user' and password: 'user123'");
        } else {
            log.info("Regular user already exists, skipping initialization");
        }
    }
}