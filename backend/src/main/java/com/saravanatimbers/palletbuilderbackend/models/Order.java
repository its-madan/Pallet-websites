package com.saravanatimbers.palletbuilderbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String orderId;
    private String userId;
    private String userName;
    private Map<String, Object> quoteDetails;
    private String status; // pending, in production, dispatched
    private Instant createdAt;
    private Instant updatedAt;
    private Instant shippedAt; // When the order was marked as shipped/dispatched

    public Order() {}

    public Order(String orderId, String userId, String userName, Map<String, Object> quoteDetails, String status, Instant createdAt, Instant updatedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.quoteDetails = quoteDetails;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Map<String, Object> getQuoteDetails() { return quoteDetails; }
    public void setQuoteDetails(Map<String, Object> quoteDetails) { this.quoteDetails = quoteDetails; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }
} 