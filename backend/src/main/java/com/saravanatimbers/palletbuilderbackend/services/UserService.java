package com.saravanatimbers.palletbuilderbackend.services;

import com.saravanatimbers.palletbuilderbackend.dto.RegisterRequest;
import com.saravanatimbers.palletbuilderbackend.models.User;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import com.saravanatimbers.palletbuilderbackend.models.Order;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    public User registerNewUser(RegisterRequest registerRequest) {
        // Only check for email existence
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        List<String> roles = (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty())
            ? registerRequest.getRoles().stream().map(role -> "ROLE_" + role.toUpperCase()).collect(Collectors.toList())
            : List.of("ROLE_USER");

        // Enforce only one admin
        if (roles.contains("ROLE_ADMIN")) {
            boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN"));
            if (adminExists) {
                throw new RuntimeException("Only one admin user is allowed.");
            }
        }

        User user = new User(
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getPhoneNumber(),
                roles
        );

        return userRepository.save(user);
    }

    // Scheduled task to update shipped orders to delivered after 48 hours
    @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
    public void updateShippedOrdersToDelivered() {
        List<Order> orders = orderRepository.findAll();
        Instant now = Instant.now();
        for (Order order : orders) {
            if ("shipped".equalsIgnoreCase(order.getStatus()) && order.getShippedAt() != null) {
                if (order.getShippedAt().plus(48, ChronoUnit.HOURS).isBefore(now)) {
                    order.setStatus("delivered");
                    order.setUpdatedAt(now);
                    orderRepository.save(order);
                }
            }
        }
    }
} 