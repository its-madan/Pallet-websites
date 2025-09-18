package com.saravanatimbers.palletbuilderbackend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Document(collection = "user_infos")
public class UserInfo {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId; // Reference to the user's username
    
    private String fullName;
    private String email;
    private String phone;
    private String company;
    private String address;
    private String state;
    private String pincode;
    private String status;
    
    // Discount category: VIP, Bulk Orders, Frequent Buyer, Special Pricing, or null
    private String discountCategory;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    public UserInfo() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public UserInfo(String userId, String fullName, String email, String phone, String company, String address, String state, String pincode) {
        this();
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.address = address;
        this.state = state;
        this.pincode = pincode;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiscountCategory() {
        return discountCategory;
    }
    public void setDiscountCategory(String discountCategory) {
        this.discountCategory = discountCategory;
    }
} 