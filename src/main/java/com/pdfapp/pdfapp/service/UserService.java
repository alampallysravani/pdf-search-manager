package com.pdfapp.pdfapp.service;

import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.repository.UserRepository;
import com.pdfapp.pdfapp.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("USER"); // Default role

        return userRepository.save(user);
    }

    public String login(String username, String rawPassword) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) throw new RuntimeException("User not found");

        User user = optUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // ✅ generate token with role embedded
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
