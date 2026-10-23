package com.nubb.classroom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role; // "ADMIN" or "TEACHER"
    private String fullName;
    private Boolean active = true;

    public Account() {}

    public Account(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.active = true;
    }

    public Account(String username, String password, String role, String fullName, Boolean active) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.active = active != null ? active : true;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }
    public boolean isActive() { return active != null && active; }
}
