package com.pdfapp.pdfapp.config;

import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin123")); // ✅ This is the CORRECT setter
                admin.setEmail("admin@example.com");
                admin.setRole("ADMIN");

                userRepository.save(admin);

                System.out.println("✅ Admin created: admin / admin123");
            }
        };
    }
}
