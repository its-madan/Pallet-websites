package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.models.Order;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.name || hasRole('ADMIN')")
    public List<Order> getOrdersForUser(@PathVariable String userId) {
        logger.info("Fetching orders for userId: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.info("Found {} orders for userId {}", orders.size(), userId);
        return orders;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        logger.info("Fetching all orders (debug endpoint)");
        List<Order> orders = orderRepository.findAll();
        logger.info("Found {} total orders", orders.size());
        return orders;
    }
} 