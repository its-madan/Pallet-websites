package com.saravanatimbers.palletbuilderbackend.dto;

import java.util.List;

public class RegisterRequest {
    private final String fullName;
    private final String email;
    private final String password;
    private final String phoneNumber;
    private final String otp;
    private final List<String> roles;

    public RegisterRequest(String fullName, String email, String password, String phoneNumber, String otp, List<String> roles) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.otp = otp;
        this.roles = roles;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOtp() {
        return otp;
    }

    public List<String> getRoles() {
        return roles;
    }
} 