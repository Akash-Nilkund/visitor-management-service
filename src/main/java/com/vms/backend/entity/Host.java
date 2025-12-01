package com.vms.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email; // <-- REQUIRED for visitor.getHost().getEmail()
    private String mobile;
    private String department;

    // Security fields
    private String username;
    private String password;
    private String role; // e.g., "HOST", "ADMIN"

    @ManyToOne
    @jakarta.persistence.JoinColumn(name = "company_id")
    private Company company;

    // ---------------------- MANUAL GETTERS & SETTERS (ADDED)
    // ----------------------
    // These ensure your code works even if Lombok fails in VS Code.

    public Long getId() { // <-- ADDED
        return id;
    }

    public void setId(Long id) { // <-- ADDED
        this.id = id;
    }

    public String getName() { // <-- ADDED
        return name;
    }

    public void setName(String name) { // <-- ADDED
        this.name = name;
    }

    public String getEmail() { // <-- ADDED (fixes your error)
        return email;
    }

    public void setEmail(String email) { // <-- ADDED
        this.email = email;
    }

    public String getMobile() { // <-- ADDED
        return mobile;
    }

    public void setMobile(String mobile) { // <-- ADDED
        this.mobile = mobile;
    }

    public String getDepartment() { // <-- ADDED
        return department;
    }

    public void setDepartment(String department) { // <-- ADDED
        this.department = department;
    }

    public Company getCompany() { // <-- ADDED
        return company;
    }

    public void setCompany(Company company) { // <-- ADDED
        this.company = company;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    // -------------------------------------------------------------------------------
}
