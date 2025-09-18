package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.models.Quote;
import com.saravanatimbers.palletbuilderbackend.repositories.QuoteRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import com.saravanatimbers.palletbuilderbackend.models.User;
import com.saravanatimbers.palletbuilderbackend.services.QuoteEmailService;
import com.saravanatimbers.palletbuilderbackend.services.UserInfoService;
import com.saravanatimbers.palletbuilderbackend.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import com.saravanatimbers.palletbuilderbackend.models.Order;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.saravanatimbers.palletbuilderbackend.repositories.AdminSettingsRepository;
import com.saravanatimbers.palletbuilderbackend.models.AdminSettings;
import com.saravanatimbers.palletbuilderbackend.services.QuotePdfService;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {
    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteEmailService quoteEmailService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private QuotePdfService quotePdfService;

    // 1. Submit new quote
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Quote> submitQuote(@RequestBody Quote quote) {
        quote.setId(null);
        // Generate quoteId: Q-yyMMdd-XXXX (XXXX = random alphanumeric)
        String datePart = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = generateRandomAlphaNumeric(4);
        quote.setQuoteId("Q-" + datePart + "-" + randomPart);
        quote.setStatus("pending");
        quote.setCreatedAt(Instant.now());
        quote.setUpdatedAt(Instant.now());
        Quote saved = quoteRepository.save(quote);
        messagingTemplate.convertAndSend("/topic/quotes", saved);
        return ResponseEntity.ok(saved);
    }

    // Helper for random alphanumeric
    private String generateRandomAlphaNumeric(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    // 2. Edit quote (if pending)
    @PutMapping("/{id}")
    @PreAuthorize("@quoteSecurity.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<?> editQuote(@PathVariable String id, @RequestBody Quote updated) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Quote existing = opt.get();
        if (!"pending".equals(existing.getStatus())) {
            return ResponseEntity.badRequest().body("Quote cannot be edited unless status is 'pending'.");
        }
        existing.setDetails(updated.getDetails());
        existing.setUpdatedAt(Instant.now());
        Quote saved = quoteRepository.save(existing);
        messagingTemplate.convertAndSend("/topic/quotes", saved);
        return ResponseEntity.ok(saved);
    }

    // 3. Get all quotes for a user
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.name || hasRole('ADMIN')")
    public List<Quote> getUserQuotes(@PathVariable String userId) {
        logger.info("[Quote] Fetching quotes for userId: {}", userId);
        List<Quote> quotes = quoteRepository.findByUserId(userId);
        logger.info("[Quote] Found {} quotes for userId {}", quotes.size(), userId);
        return quotes;
    }

    // 4. Get all quotes (admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAllQuotesDebug() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG] Authenticated user: " + (auth != null ? auth.getName() : "null"));
        System.out.println("[DEBUG] Authorities: " + (auth != null ? auth.getAuthorities() : "null"));
        logger.info("[Quote] Fetching all quotes (debug endpoint)");
        List<Quote> quotes = quoteRepository.findAll();
        logger.info("[Quote] Found {} total quotes", quotes.size());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Quote quote : quotes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", quote.getId());
            map.put("quoteId", quote.getQuoteId());
            map.put("userId", quote.getUserId());
            map.put("details", quote.getDetails());
            map.put("status", quote.getStatus());
            map.put("createdAt", quote.getCreatedAt());
            map.put("updatedAt", quote.getUpdatedAt());
            map.put("adminActionAt", quote.getAdminActionAt());
            map.put("cancellationDate", quote.getCancellationDate());
            // Attachments logic
            List<String> files = new ArrayList<>();
            try {
                Path uploadPath = Paths.get("uploads/quotes/" + quote.getId());
                if (Files.exists(uploadPath) && Files.isDirectory(uploadPath)) {
                    Files.list(uploadPath).filter(Files::isRegularFile).forEach(f -> files.add(f.getFileName().toString()));
                }
            } catch (Exception e) {
                // Ignore errors, just leave files empty
            }
            map.put("files", files);
            result.add(map);
        }
        return result;
    }

    // 5. Update quote status (approve/reject/cancel)
    @PatchMapping("/{id}/status")
    // Only the quote owner (customer) can cancel their own quote
    @PreAuthorize("#status == 'cancelled' ? @quoteSecurity.isOwner(#id, authentication) : hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Quote quote = opt.get();
        // Debug log for troubleshooting 403 errors
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String authName = (auth != null) ? auth.getName() : "null";
        System.out.println("[DEBUG] Cancel attempt: authName=" + authName + ", quote.userId=" + quote.getUserId() + ", status=" + status);
        String oldStatus = quote.getStatus();
        quote.setStatus(status);
        quote.setUpdatedAt(Instant.now());
        if ("approved".equals(status) || "rejected".equals(status)) {
            quote.setAdminActionAt(Instant.now());
        }
        if ("cancelled".equals(status)) {
            quote.setCancellationDate(Instant.now());
        }
        Quote saved = quoteRepository.save(quote);
        messagingTemplate.convertAndSend("/topic/quotes", saved);

        // Create order if approved (AFTER quote is saved and message sent)
        if ("approved".equals(status)) {
            boolean exists = orderRepository.findAll().stream().anyMatch(o -> {
                Object qid = o.getQuoteDetails() != null ? o.getQuoteDetails().get("id") : null;
                return qid != null && qid.equals(quote.getId());
            });
            if (!exists) {
                // Generate orderId: ORD-yyMMdd-XXXX (XXXX = random alphanumeric)
                String datePart = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
                String randomPart = generateRandomAlphaNumeric(4);
                String orderId = "ORD-" + datePart + "-" + randomPart;
                String userId = quote.getUserId();
                String userName = null;
                if (userId != null) {
                    userName = userRepository.findByEmail(userId).map(u -> u.getFullName()).orElse(userId);
                }
                // Ensure quoteId is present in the details map
                Map<String, Object> orderDetails = new java.util.HashMap<>(quote.getDetails() != null ? quote.getDetails() : new java.util.HashMap<>());
                orderDetails.put("quoteId", quote.getQuoteId());

                // --- Add price fields to orderDetails ---
                AdminSettings settings = adminSettingsRepository.findAll().stream().findFirst().orElse(null);
                if (settings != null) {
                    int quantity = 1;
                    try { quantity = Integer.parseInt(orderDetails.getOrDefault("quantity", "1").toString()); } catch (Exception ignored) {}
                    String palletType = orderDetails.getOrDefault("palletType", "standard").toString();
                    String material = orderDetails.getOrDefault("material", "pine").toString();
                    String urgency = orderDetails.getOrDefault("urgency", "normal").toString();

                    double basePrice = settings.getBasePalletCost() != null ? settings.getBasePalletCost().getOrDefault(palletType, 0.0) : 0.0;
                    double materialSurcharge = settings.getMaterialSurcharge() != null ? settings.getMaterialSurcharge().getOrDefault(material, 0.0) : 0.0;
                    double urgencyFee = settings.getUrgencyFee() != null ? settings.getUrgencyFee().getOrDefault(urgency, 0.0) : 0.0;
                    // Shipping is always per order (not per pallet)
                    double shipping = settings.getShippingEstimate();
                    double cgstPercent = settings.getCgstPercent();
                    double sgstPercent = settings.getSgstPercent();

                    double subtotal = (basePrice + materialSurcharge + urgencyFee) * quantity;
                    // Note: Backend doesn't handle discounts in this calculation
                    // Discounts are handled in frontend and stored in quote details
                    double subtotalAfterDiscount = subtotal; // No discount applied in backend
                    double totalBeforeGst = subtotalAfterDiscount + shipping;
                    double totalTax = totalBeforeGst * (cgstPercent + sgstPercent) / 100.0;
                    double totalPrice = totalBeforeGst + totalTax;

                    orderDetails.put("basePrice", basePrice);
                    orderDetails.put("materialSurcharge", materialSurcharge);
                    orderDetails.put("urgencyFee", urgencyFee);
                    orderDetails.put("shipping", shipping);
                    orderDetails.put("cgstPercent", cgstPercent);
                    orderDetails.put("sgstPercent", sgstPercent);
                    orderDetails.put("subtotal", subtotal);
                    orderDetails.put("subtotalAfterDiscount", subtotalAfterDiscount);
                    orderDetails.put("totalBeforeGst", totalBeforeGst);
                    orderDetails.put("totalTax", totalTax);
                    orderDetails.put("totalPrice", totalPrice);
                }
                // --- End price fields ---

                Order order = new Order(orderId, userId, userName, orderDetails, "approved", Instant.now(), Instant.now());
                orderRepository.save(order);
            }
        }
        // Respond early
        ResponseEntity<?> response = ResponseEntity.ok(saved);
        // Send email notification if status is approved/rejected/cancelled (admin)
        if (("approved".equals(status) || "rejected".equals(status)) || ("cancelled".equals(status) && !"pending".equals(oldStatus))) {
            new Thread(() -> sendEmailNotification(quote, status)).start();
        }
        return response;
    }

    private void sendEmailNotification(Quote quote, String status) {
        System.out.println("=== EMAIL NOTIFICATION DEBUG ===");
        System.out.println("Quote ID: " + quote.getQuoteId());
        System.out.println("Status: " + status);
        System.out.println("User ID: " + quote.getUserId());
        System.out.println("Quote Details: " + quote.getDetails());
        
        try {
            String email = null;
            String customerName = null;
            
            // First try to get email from UserInfo (highest priority)
            if (quote.getUserId() != null) {
                System.out.println("Checking UserInfo for email...");
                Optional<UserInfo> userInfoOpt = userInfoService.getUserInfoByUserId(quote.getUserId());
                if (userInfoOpt.isPresent()) {
                    UserInfo userInfo = userInfoOpt.get();
                    email = userInfo.getEmail();
                    customerName = userInfo.getFullName();
                    System.out.println("Found email in UserInfo: " + email);
                    System.out.println("Found customer name in UserInfo: " + customerName);
                } else {
                    System.out.println("No UserInfo found for userId: " + quote.getUserId());
                }
            }
            
            // If no email from UserInfo, try to get from quote details (for guest users)
            if (email == null && quote.getDetails() != null) {
                System.out.println("Checking quote details for email...");
                if (quote.getDetails().get("email") != null) {
                    email = quote.getDetails().get("email").toString();
                    System.out.println("Found email in quote details: " + email);
                } else {
                    System.out.println("No email found in quote details");
                }
                if (customerName == null && quote.getDetails().get("customerName") != null) {
                    customerName = quote.getDetails().get("customerName").toString();
                    System.out.println("Found customer name in quote details: " + customerName);
                } else {
                    System.out.println("No customer name found in quote details");
                }
            } else if (quote.getDetails() == null) {
                System.out.println("Quote details is null");
            }
            
            // If still no email, try to get from user record (fallback)
            if (email == null && quote.getUserId() != null) {
                System.out.println("Checking user record for email...");
                Optional<User> userOpt = userRepository.findByEmail(quote.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    email = user.getEmail();
                    System.out.println("Found email in user record: " + email);
                    if (customerName == null) {
                        customerName = user.getFullName();
                        System.out.println("Using full name from user record: " + customerName);
                    }
                } else {
                    System.out.println("User not found in database for userId: " + quote.getUserId());
                }
            } else if (email == null) {
                System.out.println("No userId provided in quote");
            }
            
            // Send email if we have an email address
            if (email != null && !email.trim().isEmpty()) {
                System.out.println("✅ Email address found: " + email);
                System.out.println("Customer name: " + customerName);
                System.out.println("Calling email service...");
                
                quoteEmailService.sendQuoteStatusEmail(
                    email, 
                    quote.getQuoteId(), 
                    status, 
                    quote.getDetails(), 
                    customerName
                );
                System.out.println("Email notification sent to " + email + " for quote " + quote.getQuoteId() + " with status: " + status);
            } else {
                System.err.println("❌ No email address found for quote " + quote.getQuoteId() + ". Cannot send notification.");
                System.err.println("Available data:");
                System.err.println("- Quote details: " + quote.getDetails());
                System.err.println("- User ID: " + quote.getUserId());
                System.err.println("- Email from UserInfo: " + (userInfoService.getUserInfoByUserId(quote.getUserId()).isPresent() ? userInfoService.getUserInfoByUserId(quote.getUserId()).get().getEmail() : "null"));
                System.err.println("- Email from details: " + (quote.getDetails() != null ? quote.getDetails().get("email") : "null"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email notification for quote " + quote.getQuoteId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== END EMAIL NOTIFICATION DEBUG ===");
    }

    // Upload file/image for a quote
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadQuoteFile(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Quote quote = opt.get();
        try {
            String uploadDir = "uploads/quotes/" + id;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            // Save file path in quote details
            quote.getDetails().put("filePath", "/" + uploadDir + "/" + fileName);
            quote.getDetails().put("fileName", fileName);
            quote.getDetails().put("fileSize", file.getSize());
            quote.setUpdatedAt(Instant.now());
            Quote saved = quoteRepository.save(quote);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    // Serve uploaded files
    @GetMapping("/files/{quoteId}/{fileName:.+}")
    public ResponseEntity<?> serveFile(@PathVariable String quoteId, @PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads/quotes/" + quoteId + "/" + fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = determineContentType(fileName);
            
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error serving file: " + e.getMessage());
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return "application/octet-stream";
        }
    }

    // Get a single quote by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuoteById(@PathVariable String id) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(opt.get());
    }

    // Download quote PDF (with uploaded image)
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> downloadQuotePdf(@PathVariable String id) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Quote quote = opt.get();
        byte[] pdfBytes = quotePdfService.generateQuotePdf(quote);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=quote-" + quote.getQuoteId() + ".pdf")
            .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    // Test endpoint to verify email service
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        try {
            System.out.println("=== TESTING EMAIL SERVICE ===");
            System.out.println("Test email address: " + email);
            
            Map<String, Object> testDetails = new HashMap<>();
            testDetails.put("palletType", "Test Pallet");
            testDetails.put("quantity", 10);
            testDetails.put("totalPrice", 5000);
            
            quoteEmailService.sendQuoteStatusEmail(
                email, 
                "TEST123", 
                "approved", 
                testDetails, 
                "Test Customer"
            );
            
            return ResponseEntity.ok("Test email sent successfully to: " + email);
        } catch (Exception e) {
            System.err.println("Test email failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Test email failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuote(@PathVariable String id) {
        Optional<Quote> opt = quoteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        quoteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
} 