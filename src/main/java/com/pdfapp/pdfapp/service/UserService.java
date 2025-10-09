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

    /**
     * Registers a new user.
     *
     * @param username username of the new user
     * @param password raw password
     * @param email    user's email
     * @return saved User entity
     * @throws RuntimeException if username already exists
     */
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password)); // encode password
        user.setEmail(email);

        return userRepository.save(user);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param username    username
     * @param rawPassword plain password
     * @return JWT token
     * @throws RuntimeException if user not found or password invalid
     */
    public String login(String username, String rawPassword) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) throw new RuntimeException("User not found");

        User user = optUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(username);
    }

    /**
     * Returns all users.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Fetches a user by username.
     *
     * @param username username
     * @return Optional of User
     */
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
