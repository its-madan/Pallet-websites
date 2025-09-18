package com.saravanatimbers.palletbuilderbackend.dto;

import java.util.List;

public class LoginResponse {
    private final String token;
    private final List<String> roles;
    private final String username;
    private final String fullName;
    private final String email;
    private final String phoneNumber;

    public LoginResponse(String token, List<String> roles, String username, String fullName, String email, String phoneNumber) {
        this.token = token;
        this.roles = roles;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
} 