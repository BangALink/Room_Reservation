package com.example.pat_act5;

public class User {
    private String id;
    private String email;
    private String name;
    private String department;
    private String role;

    public User() {
    }

    public User(String id, String email, String name, String department, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.role = role;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
