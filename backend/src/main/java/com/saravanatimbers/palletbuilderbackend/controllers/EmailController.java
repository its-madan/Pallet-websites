package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String subject = body.get("subject");
        String message = body.get("message");
        if (to == null || subject == null || message == null || to.trim().isEmpty() || subject.trim().isEmpty() || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("To, subject, and message are required");
        }
        try {
            emailService.sendAdminEmail(to, subject, message);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }
} 