package com.pdfapp.pdfapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.repository.UserRepository;

@SpringBootApplication
public class PdfappApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfappApplication.class, args);
    }

    // ✅ Default admin creation
    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User user = new User();
                user.setUsername("admin");
                user.setPasswordHash(passwordEncoder.encode("admin123"));
                user.setEmail("admin@example.com");
                userRepository.save(user);
                System.out.println("✅ Default user 'admin' created!");
            }
        };
    }

    // ✅ Global CORS for React frontend
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
