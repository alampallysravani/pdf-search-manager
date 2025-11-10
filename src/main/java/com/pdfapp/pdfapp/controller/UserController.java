package com.pdfapp.pdfapp.controller;

import com.pdfapp.pdfapp.dto.LoginRequest;
import com.pdfapp.pdfapp.dto.LoginResponse;
import com.pdfapp.pdfapp.dto.RegisterRequest;
import com.pdfapp.pdfapp.dto.UserDTO;
import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User savedUser = userService.register(req.getUsername(), req.getPassword(), req.getEmail());
            return ResponseEntity.ok(new UserDTO(savedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // Login and return JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = userService.login(req.getUsername(), req.getPassword());
            User user = userService.getByUsername(req.getUsername()).get();

            return ResponseEntity.ok(
                new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
                )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }




    // Get all users (secured)
    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAllUsers()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    // Get user by username (secured)
    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getByUsername(@PathVariable String username) {
        return userService.getByUsername(username)
                .map(UserDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
