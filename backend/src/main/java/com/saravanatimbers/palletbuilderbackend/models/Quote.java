package com.saravanatimbers.palletbuilderbackend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "quotes")
public class Quote {
    @Id
    private String id;
    private String quoteId;
    private String userId;
    private Map<String, Object> details; // e.g., quantity, dimensions, material, etc.
    private String status; // pending, approved, rejected, cancelled
    private Instant createdAt;
    private Instant updatedAt;
    private Instant adminActionAt;
    private Instant cancellationDate;
} 