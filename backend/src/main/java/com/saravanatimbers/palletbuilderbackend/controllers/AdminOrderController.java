package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.models.Order;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import com.saravanatimbers.palletbuilderbackend.services.QuoteEmailService;
import com.saravanatimbers.palletbuilderbackend.services.InvoiceService;
import com.saravanatimbers.palletbuilderbackend.services.EmailService;
import com.saravanatimbers.palletbuilderbackend.services.UserInfoService;
import com.saravanatimbers.palletbuilderbackend.models.UserInfo;
import com.saravanatimbers.palletbuilderbackend.repositories.AdminSettingsRepository;
import com.saravanatimbers.palletbuilderbackend.models.AdminSettings;
import com.saravanatimbers.palletbuilderbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuoteEmailService quoteEmailService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private AdminSettingsRepository adminSettingsRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(orderRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch orders: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(opt.get());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestParam String status) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Order order = opt.get();
        // --- Tag-based Discount Logic on status change ---
        // (Removed: all tag-based discount logic)
        if ("in production".equalsIgnoreCase(status)) {
            order.setStatus("in production");
            order.setUpdatedAt(Instant.now());
        } else if ("dispatched".equalsIgnoreCase(status)) {
            order.setStatus("shipped");
            order.setShippedAt(Instant.now());
            order.setUpdatedAt(Instant.now());
        } else if ("shipped".equalsIgnoreCase(status)) {
            // Prevent direct update to shipped (should only be set by system)
            return ResponseEntity.badRequest().body("Cannot set status to 'shipped' directly.");
        } else {
            order.setStatus(status);
            order.setUpdatedAt(Instant.now());
        }
        orderRepository.save(order);
        // Trigger discount category re-evaluation for the user
        if (order.getUserId() != null) {
            userInfoService.assignDiscountCategory(userInfoService.getUserInfoByUserId(order.getUserId()).orElse(null));
        }
        // (Removed: recalculateAndUpdateTagsForUser)
        // Prepare email notification to user
        final String email;
        final String customerName = order.getUserName();
        if (order.getUserId() != null) {
            email = userRepository.findByEmail(order.getUserId()).map(u -> u.getEmail()).orElse(null);
        } else {
            email = null;
        }
        final String quoteId = order.getQuoteDetails() != null && order.getQuoteDetails().get("quoteId") != null ? order.getQuoteDetails().get("quoteId").toString() : order.getOrderId();
        final String orderStatus = status;
        final Map<String, Object> orderDetails = order.getQuoteDetails();
        ResponseEntity<?> response = ResponseEntity.ok(order);
        if (email != null && !email.isEmpty()) {
            // Send status email asynchronously
            String idForEmail = ("in production".equalsIgnoreCase(status) || "dispatched".equalsIgnoreCase(status)) ? order.getOrderId() : quoteId;
            new Thread(() -> quoteEmailService.sendQuoteStatusEmail(email, idForEmail, orderStatus, orderDetails, customerName)).start();
            // If dispatched, also send invoice
            if ("dispatched".equalsIgnoreCase(status)) {
                new Thread(() -> {
                    try {
                        byte[] invoicePdf = invoiceService.generateInvoicePdf(order);
                        emailService.sendInvoiceToCustomer(email, invoicePdf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        return response;
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersForUser(@PathVariable String userId) {
        return orderRepository.findByUserId(userId);
    }
} 