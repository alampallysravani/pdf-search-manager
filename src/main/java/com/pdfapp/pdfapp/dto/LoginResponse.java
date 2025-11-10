package com.pdfapp.pdfapp.dto;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private String role;

    public LoginResponse(String token, Long id, String username, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
