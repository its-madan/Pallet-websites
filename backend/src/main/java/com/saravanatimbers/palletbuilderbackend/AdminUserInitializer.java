package com.saravanatimbers.palletbuilderbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import java.util.Collections;
import com.saravanatimbers.palletbuilderbackend.models.User;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminPassword = "adm@123";
        String adminEmail = "saravanatimbers.web@gmail.com";
        String adminFullName = "Administrator";
        String adminPhone = "1234567890";

        logger.info("Initializing admin user with email: {}", adminEmail);
        
        var adminOpt = userRepository.findByEmail(adminEmail);
        User admin;
        if (adminOpt.isEmpty()) {
            logger.info("Creating new admin user");
            admin = new User(
                adminFullName,
                adminEmail,
                passwordEncoder.encode(adminPassword),
                adminPhone,
                Collections.singletonList("ROLE_ADMIN")
            );
            admin.setEmailVerified(true);
            userRepository.save(admin);
            logger.info("Default admin user created successfully");
        } else {
            logger.info("Updating existing admin user");
            admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(adminPassword)); // Always reset password
            admin.setRoles(Collections.singletonList("ROLE_ADMIN"));
            admin.setEmailVerified(true);
            admin.setFullName(adminFullName);
            admin.setPhoneNumber(adminPhone);
            userRepository.save(admin);
            logger.info("Admin user updated successfully");
        }
        
        // Verify the admin user was saved correctly
        var savedAdmin = userRepository.findByEmail(adminEmail);
        if (savedAdmin.isPresent()) {
            logger.info("Admin user verification successful - Email: {}, Roles: {}", 
                savedAdmin.get().getEmail(), savedAdmin.get().getRoles());
        } else {
            logger.error("Failed to verify admin user creation");
        }
    }
} 