package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.dto.AuthRequest;
import com.saravanatimbers.palletbuilderbackend.dto.LoginResponse;
import com.saravanatimbers.palletbuilderbackend.dto.RegisterRequest;
import com.saravanatimbers.palletbuilderbackend.models.User;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import com.saravanatimbers.palletbuilderbackend.security.JwtUtil;
import com.saravanatimbers.palletbuilderbackend.services.MongoUserDetailsService;
import com.saravanatimbers.palletbuilderbackend.services.OtpEmailService;
import com.saravanatimbers.palletbuilderbackend.services.UserService;
import com.saravanatimbers.palletbuilderbackend.services.PasswordResetEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;
import java.security.SecureRandom;
import java.math.BigInteger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MongoUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpEmailService otpEmailService;

    @Autowired
    private PasswordResetEmailService passwordResetEmailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequest registerRequest) {
        logger.info("Signup request received for email: {}", registerRequest.getEmail());
        
        // Check if user already exists
        var userOpt = userRepository.findByEmail(registerRequest.getEmail());
        if (userOpt.isPresent()) {
            logger.warn("Signup failed - Email already exists: {}", registerRequest.getEmail());
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        // For testing purposes, skip email verification
        // TODO: Re-enable email verification in production
        /*
        if (!otpEmailService.isEmailVerified(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email not verified. Please verify your email before signing up.");
        }
        */
        
        try {
            logger.info("Creating new user: {}", registerRequest.getEmail());
            User user = userService.registerNewUser(registerRequest);
            user.setEmailVerified(true);
            userRepository.save(user);
            // otpEmailService.clearVerifiedEmail(registerRequest.getEmail());
            logger.info("User registered successfully: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            logger.error("Signup failed for user {}: {}", registerRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        logger.info("AuthController: /signin endpoint called with email: {}", authRequest.getEmail());
        try {
            logger.info("Attempting to authenticate user with email: {}", authRequest.getEmail());
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            logger.info("Authentication successful for email: {}", authRequest.getEmail());
        } catch (Exception e) {
            logger.error("Authentication failed for email: {} - Error: {}", authRequest.getEmail(), e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid email or password: " + e.getMessage());
        }
        try {
            logger.info("Loading user details for email: {}", authRequest.getEmail());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            final User user = userRepository.findByEmail(authRequest.getEmail()).orElse(null);
            if (user == null) {
                logger.error("User not found in database for email: {}", authRequest.getEmail());
                return ResponseEntity.status(401).body("User not found");
            }
            logger.info("Generating JWT token for user with email: {}", authRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);
            final List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            logger.info("Login successful for user with email: {} and roles: {}", authRequest.getEmail(), roles);
            // Use all roles for LoginResponse to match frontend expectations
            return ResponseEntity.ok(new LoginResponse(
                jwt,
                roles,
                user.getEmail(), // username (using email as username)
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber()
            ));
        } catch (Exception e) {
            logger.error("Error during login process for email: {} - Error: {}", authRequest.getEmail(), e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error during login");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Warn user to enter a registered email address
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please enter a registered email address."));
        }
        // Generate secure random token
        SecureRandom random = new SecureRandom();
        String token = new BigInteger(130, random).toString(32);
        long expiry = System.currentTimeMillis() + 30 * 60 * 1000; // 30 minutes
        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        passwordResetEmailService.sendPasswordResetEmail(email, resetLink);
        return ResponseEntity.ok(Map.of("success", true, "message", "If the email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (!StringUtils.hasText(token) || !StringUtils.hasText(newPassword)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Token and new password are required"));
        }
        User user = userRepository.findAll().stream()
            .filter(u -> token.equals(u.getResetToken()))
            .findFirst().orElse(null);
        if (user == null || user.getResetTokenExpiry() == null || user.getResetTokenExpiry() < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid or expired token"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password has been reset successfully."));
    }
} 