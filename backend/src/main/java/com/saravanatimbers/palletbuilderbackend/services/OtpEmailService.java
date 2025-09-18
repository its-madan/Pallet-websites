package com.saravanatimbers.palletbuilderbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;

@Service
public class OtpEmailService {
    private static final Logger logger = LoggerFactory.getLogger(OtpEmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastOtpRequest = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // OTP expires after 10 minutes
    private static final int OTP_EXPIRY_MINUTES = 10;
    // Rate limiting: minimum 30 seconds between OTP requests for same email
    private static final int RATE_LIMIT_SECONDS = 30;

    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;
        
        OtpData(String otp) {
            this.otp = otp;
            this.expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public boolean sendOtp(String email) {
        try {
            // Check rate limiting
            LocalDateTime lastRequest = lastOtpRequest.get(email);
            if (lastRequest != null) {
                long secondsSinceLastRequest = java.time.Duration.between(lastRequest, LocalDateTime.now()).getSeconds();
                if (secondsSinceLastRequest < RATE_LIMIT_SECONDS) {
                    logger.warn("Rate limit exceeded for email: {} ({} seconds since last request)", email, secondsSinceLastRequest);
                    return false;
                }
            }
            
            // Generate 6-digit OTP
            String otp = String.format("%06d", random.nextInt(999999));
            
            // Store OTP with expiry time immediately
            otpStorage.put(email, new OtpData(otp));
            
            // Update last request time
            lastOtpRequest.put(email, LocalDateTime.now());
            
            // Send email asynchronously
            sendEmailAsync(email, otp);
            
            logger.info("OTP generated and stored for email: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Error generating OTP for email: {}", email, e);
            // Remove the OTP if generation fails
            otpStorage.remove(email);
            return false;
        }
    }

    @Async
    protected void sendEmailAsync(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Your Email Verification Code - Saravana Timbers");
            helper.setFrom("saravanatimbers.web@gmail.com");
            
            // Try to embed logo with better error handling
            try {
                ClassPathResource logoResource = new ClassPathResource("static/images/company-logo.png");
                if (logoResource.exists()) {
                    helper.addInline("logo", logoResource);
                    logger.info("✅ Logo embedded successfully in OTP email");
                } else {
                    logger.warn("⚠️ Logo file not found at: static/images/company-logo.png");
                }
            } catch (Exception logoException) {
                logger.error("❌ Failed to embed logo in OTP email: " + logoException.getMessage());
                logoException.printStackTrace();
            }
            
            String body = "<div style='font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f7;border-radius:16px;box-shadow:0 2px 12px rgba(44,44,44,0.07);overflow:hidden;'>" +
                "<div style='background:#ffe082;padding:20px 0 16px 0;text-align:center;border-bottom:1px solid #f3e5ab;border-radius:0 0 16px 16px;'>" +
                "<span style='font-size:2rem;font-weight:700;color:#232b39;letter-spacing:1px;display:block;'>SARAVANA TIMBERS</span>" +
                "<span style='color:#232b39;font-size:1rem;display:block;margin-top:4px;'>GSTIN: 33ASPPS0683QIZU &nbsp; | &nbsp; Phone: 9788885558</span>" +
                "<span style='color:#232b39;font-size:0.95rem;display:block;margin-top:2px;'>No 7,Thudiyalur road, Saravanampatti, Coimbatore - 641035</span>" +
                "</div>" +
                "<div style='height:1px;background:#eee;'></div>" +
                "<div style='padding:32px 24px 24px 24px;background:#fff;border-radius:0 0 16px 16px;'>" +
                "<div style='font-size:1.1rem;color:#232b39;'>" +
                "<p>Hello,</p>" +
                "<p>Your email verification code is: <b>" + otp + "</b></p>" +
                "<p>This code will expire in 10 minutes.</p>" +
                "<p>If you didn't request this code, please ignore this email.</p>" +
                "<p>Best regards,<br/>Saravana Timbers Team</p>" +
                "</div>" +
                "<div style='margin-top:2.5rem;padding-top:1.5rem;border-top:1px solid #eee;color:#888;font-size:0.95rem;'>" +
                "📞 +91-9788885558 &nbsp; | &nbsp; 🌐 www.saravanatimbers.com &nbsp; | &nbsp; ✉ saravanatimbers.web@gmail.com" +
                "<div style='margin-top:0.5rem;color:#aaa;font-size:0.9rem;'>Thank you for choosing Saravana Timbers!</div>" +
                "</div></div></div>";
            helper.setText(body, true);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", email, e);
        }
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            return false; // No OTP found for this email
        }
        
        // Check if OTP is expired
        if (otpData.isExpired()) {
            otpStorage.remove(email);
            return false;
        }
        
        // Check if OTP matches
        if (otpData.otp.equals(otp)) {
            otpStorage.remove(email); // Remove OTP after successful verification
            verifiedEmails.put(email, true); // Mark email as verified
            lastOtpRequest.remove(email); // Clear rate limiting for this email
            logger.info("Email verified successfully: {}", email);
            return true;
        }
        
        return false;
    }
    
    // Scheduled cleanup every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledCleanup() {
        cleanupExpiredOtps();
    }
    
    // Method to clean up expired OTPs and old rate limit entries
    public void cleanupExpiredOtps() {
        int beforeSize = otpStorage.size();
        otpStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = otpStorage.size();
        if (beforeSize != afterSize) {
            logger.info("Cleaned up {} expired OTPs", beforeSize - afterSize);
        }
        
        // Clean up old rate limit entries (older than 1 hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        lastOtpRequest.entrySet().removeIf(entry -> entry.getValue().isBefore(oneHourAgo));
    }

    public boolean isEmailVerified(String email) {
        return verifiedEmails.getOrDefault(email, false);
    }

    public void clearVerifiedEmail(String email) {
        verifiedEmails.remove(email);
    }
    
    // Method to check if email is rate limited
    public boolean isRateLimited(String email) {
        LocalDateTime lastRequest = lastOtpRequest.get(email);
        if (lastRequest == null) {
            return false;
        }
        long secondsSinceLastRequest = java.time.Duration.between(lastRequest, LocalDateTime.now()).getSeconds();
        return secondsSinceLastRequest < RATE_LIMIT_SECONDS;
    }

    // Method to send admin email
    @Async
    public void sendAdminEmail(String to, String subject, String message) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("saravanatimbers.web@gmail.com");
        helper.setText(message, true);
        mailSender.send(mimeMessage);
    }
} 