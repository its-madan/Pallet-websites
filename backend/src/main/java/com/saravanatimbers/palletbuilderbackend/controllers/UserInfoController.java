package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.models.UserInfo;
import com.saravanatimbers.palletbuilderbackend.services.UserInfoService;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-info")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class UserInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserRepository userRepository;

    // Allow dot characters in email path variable
    @GetMapping("/{userId:.+}")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[UserInfoController] GET /user-info/{} called by {}", userId, (auth != null ? auth.getName() : "null"));
        if (auth == null || !auth.getName().equals(userId)) {
            logger.warn("[UserInfoController] Access denied for userId: {}", userId);
            return ResponseEntity.status(403).body("Access denied");
        }
        Optional<UserInfo> userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo.isPresent()) {
            logger.info("[UserInfoController] Found user info for userId: {}: {}", userId, userInfo.get());
            return ResponseEntity.ok(userInfo.get());
        } else {
            logger.warn("[UserInfoController] No user info found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUserInfo(@RequestBody UserInfo userInfo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[UserInfoController] POST /user-info for userId: {} by {}. Data: {}", userInfo.getUserId(), (auth != null ? auth.getName() : "null"), userInfo);
        // Security check: ensure user can only create info for themselves
        if (auth == null || !auth.getName().equals(userInfo.getUserId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        // Validation
        if (userInfo.getFullName() == null || userInfo.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Full name is required");
        }
        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (userInfo.getPhone() == null || userInfo.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone is required");
        }
        if (userInfo.getAddress() == null || userInfo.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Address is required");
        }
        if (userInfo.getState() == null || userInfo.getState().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("State is required");
        }
        if (userInfo.getPincode() == null || userInfo.getPincode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Pincode is required");
        }

        // Email format validation
        if (!userInfo.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        // Phone number validation (basic)
        if (!userInfo.getPhone().matches("^[0-9]{10}$")) {
            return ResponseEntity.badRequest().body("Phone number must be 10 digits");
        }

        // Pincode validation (6 digits)
        if (!userInfo.getPincode().matches("^[0-9]{6}$")) {
            return ResponseEntity.badRequest().body("Pincode must be 6 digits");
        }

        try {
            UserInfo savedUserInfo = userInfoService.saveUserInfo(userInfo);
            logger.info("[UserInfoController] Saved user info: {}", savedUserInfo);
            return ResponseEntity.ok(savedUserInfo);
        } catch (Exception e) {
            logger.error("[UserInfoController] Failed to save user info: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to save user info: " + e.getMessage());
        }
    }

    // Allow dot characters in email path variable
    @PutMapping("/{userId:.+}")
    public ResponseEntity<?> updateUserInfo(@PathVariable String userId, @RequestBody UserInfo userInfo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[UserInfoController] PUT /user-info/{} by {}. Data: {}", userId, (auth != null ? auth.getName() : "null"), userInfo);
        // Security check: ensure user can only update their own info
        if (auth == null || !auth.getName().equals(userId)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        // Validation
        if (userInfo.getFullName() == null || userInfo.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Full name is required");
        }
        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (userInfo.getPhone() == null || userInfo.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone is required");
        }
        if (userInfo.getAddress() == null || userInfo.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Address is required");
        }
        if (userInfo.getState() == null || userInfo.getState().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("State is required");
        }
        if (userInfo.getPincode() == null || userInfo.getPincode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Pincode is required");
        }

        // Email format validation
        if (!userInfo.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        // Phone number validation (basic)
        if (!userInfo.getPhone().matches("^[0-9]{10}$")) {
            return ResponseEntity.badRequest().body("Phone number must be 10 digits");
        }

        // Pincode validation (6 digits)
        if (!userInfo.getPincode().matches("^[0-9]{6}$")) {
            return ResponseEntity.badRequest().body("Pincode must be 6 digits");
        }

        try {
            UserInfo updatedUserInfo = userInfoService.updateUserInfo(userId, userInfo);
            logger.info("[UserInfoController] Updated user info: {}", updatedUserInfo);
            return ResponseEntity.ok(updatedUserInfo);
        } catch (Exception e) {
            logger.error("[UserInfoController] Failed to update user info: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update user info: " + e.getMessage());
        }
    }

    // Admin-only endpoint to override/set discount category
    // Allow dot characters in email path variable
    @PutMapping("/{userId:.+}/discount-category")
    public ResponseEntity<?> setDiscountCategory(@PathVariable String userId, @RequestBody java.util.Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[UserInfoController] Authorities for user {}: {}", auth.getName(), auth.getAuthorities());
        // Check if user is admin
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        String category = body.get("discountCategory");
        logger.info("[UserInfoController] Received discount category request: category='{}', body={}", category, body);
        if (category == null) {
            return ResponseEntity.badRequest().body("discountCategory is required");
        }
        Optional<UserInfo> userInfoOpt = userInfoService.getUserInfoByUserId(userId);
        if (userInfoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserInfo userInfo = userInfoOpt.get();
        logger.info("[UserInfoController] Setting discount category for user {}: {} -> {}", userId, userInfo.getDiscountCategory(), category);
        userInfo.setDiscountCategory(category.isEmpty() ? null : category);
        // Use updateUserInfo to ensure proper handling of manually set discount categories
        // Pass a flag to indicate this is a manual admin override
        UserInfo updatedUserInfo = userInfoService.updateUserInfoWithManualDiscountOverride(userId, userInfo);
        logger.info("[UserInfoController] Updated user discount category: {}", updatedUserInfo.getDiscountCategory());
        return ResponseEntity.ok(updatedUserInfo);
    }

    // Allow dot characters in email path variable
    @GetMapping("/{userId:.+}/exists")
    public ResponseEntity<?> checkUserInfoExists(@PathVariable String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("[UserInfoController] GET /user-info/{}/exists called by {}", userId, (auth != null ? auth.getName() : "null"));
        // Security check: ensure user can only check their own info
        if (auth == null || !auth.getName().equals(userId)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        boolean exists = userInfoService.userInfoExists(userId);
        logger.info("[UserInfoController] /exists result for userId {}: {}", userId, exists);
        return ResponseEntity.ok(new UserInfoExistsResponse(exists));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUserInfos() {
        logger.info("[UserInfo] Fetching all user infos (debug endpoint)");
        try {
            java.util.List<com.saravanatimbers.palletbuilderbackend.models.UserInfo> all = userInfoService.getAllUserInfos();
            // Filter out administrators
            java.util.List<com.saravanatimbers.palletbuilderbackend.models.UserInfo> filtered = all.stream()
                .filter(userInfo -> {
                    // Check if user has ADMIN role
                    java.util.Optional<com.saravanatimbers.palletbuilderbackend.models.User> userOpt = userRepository.findByEmail(userInfo.getUserId());
                    if (userOpt.isPresent()) {
                        com.saravanatimbers.palletbuilderbackend.models.User user = userOpt.get();
                        return user.getRoles() == null || !user.getRoles().contains("ROLE_ADMIN");
                    }
                    return true; // Include if user not found (shouldn't happen)
                })
                .collect(java.util.stream.Collectors.toList());
            logger.info("[UserInfo] Found {} user infos ({} after filtering out admins)", all.size(), filtered.size());
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            logger.error("[UserInfo] Failed to fetch all user infos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch user infos: " + e.getMessage());
        }
    }

    // Helper class for response
    private static class UserInfoExistsResponse {
        private boolean exists;

        public UserInfoExistsResponse(boolean exists) {
            this.exists = exists;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }
    }
} 