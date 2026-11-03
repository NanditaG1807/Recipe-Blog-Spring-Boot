package com.main.recipeblog.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.main.recipeblog.entity.Role;
import com.main.recipeblog.entity.User;
import com.main.recipeblog.repository.UserRepository;

@Configuration
public class DatabaseInitializer {

    @Bean
    public CommandLineRunner initializeDatabase(
            UserRepository repo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create only ONE ADMIN
            if (repo.findByEmail("admin@gmail.com").isEmpty()) {

                User admin = new User(
                        "admin@gmail.com",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );

                repo.save(admin);

                System.out.println("Admin created");
            }

        };
    }
}
