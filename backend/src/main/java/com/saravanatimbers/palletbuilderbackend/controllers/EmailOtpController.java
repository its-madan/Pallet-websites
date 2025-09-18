package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.services.OtpEmailService;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import com.saravanatimbers.palletbuilderbackend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class EmailOtpController {

    @Autowired
    private OtpEmailService otpEmailService;

    @Autowired
    private UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @PostMapping("/send-email-otp")
    public ResponseEntity<?> sendEmailOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            
            // Validation
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"
                ));
            }
            
            email = email.trim().toLowerCase();
            
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please enter a valid email address"
                ));
            }
            
            // Check rate limiting
            if (otpEmailService.isRateLimited(email)) {
                return ResponseEntity.status(429).body(Map.of(
                    "success", false,
                    "message", "Please wait 30 seconds before requesting another OTP"
                ));
            }
            
            // Send OTP
            boolean sent = otpEmailService.sendOtp(email);
            
            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent successfully to your email"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP. Please try again."
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "An error occurred while sending OTP"
            ));
        }
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<?> verifyEmailOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");
            
            // Validation
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"
                ));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "OTP is required"
                ));
            }
            
            email = email.trim().toLowerCase();
            otp = otp.trim();
            
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please enter a valid email address"
                ));
            }
            
            if (otp.length() != 6 || !otp.matches("\\d{6}")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please enter a valid 6-digit OTP"
                ));
            }
            
            // Verify OTP
            boolean valid = otpEmailService.verifyOtp(email, otp);
            
            if (valid) {
                // Set emailVerified to true if user exists
                userRepository.findByEmail(email).ifPresent(user -> {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                });
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid OTP. Please check and try again."
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "An error occurred while verifying OTP"
            ));
        }
    }

    // Test endpoint to verify OTP service is working (for development/testing)
    @GetMapping("/test-email-otp")
    public ResponseEntity<?> testEmailOtp() {
        try {
            // Test email generation and storage
            String testEmail = "test@example.com";
            boolean sent = otpEmailService.sendOtp(testEmail);
            
            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email OTP service is working correctly",
                    "testEmail", testEmail,
                    "note", "Check the console/logs for the generated OTP"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Email OTP service is not working"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error testing email OTP service: " + e.getMessage()
            ));
        }
    }
} 