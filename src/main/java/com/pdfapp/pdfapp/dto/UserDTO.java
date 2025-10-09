package com.pdfapp.pdfapp.dto;

import com.pdfapp.pdfapp.model.User;

public class UserDTO {
    private Long id;
    private String username;
    private String email;

    public UserDTO() {}
    public UserDTO(User u) {
        if (u != null) {
            this.id = u.getId();
            this.username = u.getUsername();
            this.email = u.getEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
