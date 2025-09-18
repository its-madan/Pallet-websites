package com.saravanatimbers.palletbuilderbackend;

import com.saravanatimbers.palletbuilderbackend.models.Order;
import com.saravanatimbers.palletbuilderbackend.models.Quote;
import com.saravanatimbers.palletbuilderbackend.models.User;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.QuoteRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderUserNameMigration implements CommandLineRunner {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== STARTING DATA MIGRATION ===");
        
        // Step 1: Build username to email mapping
        Map<String, String> usernameToEmail = buildUsernameToEmailMapping();
        System.out.println("Username to email mapping: " + usernameToEmail);
        
        // Step 2: Migrate quotes
        migrateQuotes(usernameToEmail);
        
        // Step 3: Migrate orders
        migrateOrders(usernameToEmail);
        
        // Step 4: Clean up orphaned data
        cleanupOrphanedData();
        
        System.out.println("=== DATA MIGRATION COMPLETED ===");
    }

    private Map<String, String> buildUsernameToEmailMapping() {
        Map<String, String> mapping = new HashMap<>();
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            // If user has both username and email, map them
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                // For users that might have had username field, map it to email
                // Since we removed username, we'll use email as the key for consistency
                mapping.put(user.getEmail(), user.getEmail());
            }
        }
        
        return mapping;
    }

    private void migrateQuotes(Map<String, String> usernameToEmail) {
        System.out.println("Migrating quotes...");
        List<Quote> quotes = quoteRepository.findAll();
        int migratedCount = 0;
        int orphanedCount = 0;
        
        for (Quote quote : quotes) {
            String userId = quote.getUserId();
            
            if (userId == null || userId.trim().isEmpty()) {
                // Quote has no userId - mark as orphaned
                System.out.println("Orphaned quote found: " + quote.getQuoteId() + " (no userId)");
                orphanedCount++;
                continue;
            }
            
            // Check if userId is already an email
            if (userId.contains("@")) {
                // Already an email, no migration needed
                System.out.println("Quote " + quote.getQuoteId() + " already uses email as userId: " + userId);
                continue;
            }
            
            // Check if we can map this username to an email
            String email = usernameToEmail.get(userId);
            if (email != null) {
                quote.setUserId(email);
                quoteRepository.save(quote);
                System.out.println("Migrated quote " + quote.getQuoteId() + ": " + userId + " -> " + email);
                migratedCount++;
            } else {
                // Username not found in mapping - mark as orphaned
                System.out.println("Orphaned quote found: " + quote.getQuoteId() + " (unknown userId: " + userId + ")");
                orphanedCount++;
            }
        }
        
        System.out.println("Quotes migration completed: " + migratedCount + " migrated, " + orphanedCount + " orphaned");
    }

    private void migrateOrders(Map<String, String> usernameToEmail) {
        System.out.println("Migrating orders...");
        List<Order> orders = orderRepository.findAll();
        int migratedCount = 0;
        int orphanedCount = 0;
        
        for (Order order : orders) {
            String userId = order.getUserId();
            
            if (userId == null || userId.trim().isEmpty()) {
                // Order has no userId - mark as orphaned
                System.out.println("Orphaned order found: " + order.getOrderId() + " (no userId)");
                orphanedCount++;
                continue;
            }
            
            // Check if userId is already an email
            if (userId.contains("@")) {
                // Already an email, no migration needed
                System.out.println("Order " + order.getOrderId() + " already uses email as userId: " + userId);
                continue;
            }
            
            // Check if we can map this username to an email
            String email = usernameToEmail.get(userId);
            if (email != null) {
                order.setUserId(email);
                orderRepository.save(order);
                System.out.println("Migrated order " + order.getOrderId() + ": " + userId + " -> " + email);
                migratedCount++;
            } else {
                // Username not found in mapping - mark as orphaned
                System.out.println("Orphaned order found: " + order.getOrderId() + " (unknown userId: " + userId + ")");
                orphanedCount++;
            }
        }
        
        System.out.println("Orders migration completed: " + migratedCount + " migrated, " + orphanedCount + " orphaned");
    }

    private void cleanupOrphanedData() {
        System.out.println("Cleaning up orphaned data...");
        
        // Find and report orphaned quotes (quotes with null or invalid userId)
        List<Quote> orphanedQuotes = quoteRepository.findAll().stream()
            .filter(q -> q.getUserId() == null || q.getUserId().trim().isEmpty() || !q.getUserId().contains("@"))
            .collect(Collectors.toList());
        
        if (!orphanedQuotes.isEmpty()) {
            System.out.println("Found " + orphanedQuotes.size() + " orphaned quotes:");
            for (Quote quote : orphanedQuotes) {
                System.out.println("  - Quote ID: " + quote.getQuoteId() + ", UserId: " + quote.getUserId());
            }
        }
        
        // Find and report orphaned orders (orders with null or invalid userId)
        List<Order> orphanedOrders = orderRepository.findAll().stream()
            .filter(o -> o.getUserId() == null || o.getUserId().trim().isEmpty() || !o.getUserId().contains("@"))
            .collect(Collectors.toList());
        
        if (!orphanedOrders.isEmpty()) {
            System.out.println("Found " + orphanedOrders.size() + " orphaned orders:");
            for (Order order : orphanedOrders) {
                System.out.println("  - Order ID: " + order.getOrderId() + ", UserId: " + order.getUserId());
            }
        }
        
        System.out.println("Cleanup report completed. Orphaned data found: " + 
            orphanedQuotes.size() + " quotes, " + orphanedOrders.size() + " orders");
    }
} 