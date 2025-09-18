package com.saravanatimbers.palletbuilderbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.io.ClassPathResource;

@Service
public class QuoteEmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendQuoteStatusEmail(String to, String quoteId, String status, Map<String, Object> quoteDetails, String customerName) {
        System.out.println("=== EMAIL SERVICE DEBUG ===");
        System.out.println("Attempting to send email to: " + to);
        System.out.println("Quote ID: " + quoteId);
        System.out.println("Status: " + status);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Quote Details: " + quoteDetails);

        String subject = "Quote Status Update - Saravana Timbers";
        String body = buildEmailBody(quoteId, status, quoteDetails, customerName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("saravanatimbers.web@gmail.com");
            
            // Try to embed logo with better error handling
            try {
                ClassPathResource logoResource = new ClassPathResource("static/images/company-logo.png");
                if (logoResource.exists()) {
                    helper.addInline("logo", logoResource);
                    System.out.println("✅ Logo embedded successfully");
                } else {
                    System.out.println("⚠️ Logo file not found at: static/images/company-logo.png");
                }
            } catch (Exception logoException) {
                System.err.println("❌ Failed to embed logo: " + logoException.getMessage());
                logoException.printStackTrace();
            }
            
            helper.setText(body, true); // true = HTML
            mailSender.send(message);
            System.out.println("✅ EMAIL SENT SUCCESSFULLY!");
        } catch (Exception e) {
            System.err.println("❌ EMAIL SENDING FAILED!");
            System.err.println("Failed to send email to: " + to + " for quote: " + quoteId + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== END EMAIL SERVICE DEBUG ===");
    }

    private String buildEmailBody(String quoteId, String status, Map<String, Object> quoteDetails, String customerName) {
        // --- HTML template with branding ---
        StringBuilder body = new StringBuilder();
        // Remove logo section, start with card container
        body.append("<div style='font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f7;border-radius:16px;box-shadow:0 2px 12px rgba(44,44,44,0.07);overflow:hidden;'>");
        // Header section (soft yellow, rounded bottom corners)
        body.append("<div style='background:#ffe082;padding:20px 0 16px 0;text-align:center;border-bottom:1px solid #f3e5ab;border-radius:0 0 16px 16px;'>");
        body.append("<span style='font-size:2rem;font-weight:700;color:#232b39;letter-spacing:1px;display:block;'>SARAVANA TIMBERS</span>");
        body.append("<span style='color:#232b39;font-size:1rem;display:block;margin-top:4px;'>GSTIN: 33ASPPS0683QIZU &nbsp; | &nbsp; Phone: 9788885558</span>");
        body.append("<span style='color:#232b39;font-size:0.95rem;display:block;margin-top:2px;'>No 7,Thudiyalur road, Saravanampatti, Coimbatore - 641035</span>");
        body.append("</div>");
        // Divider
        body.append("<div style='height:1px;background:#eee;'></div>");
        // Main content
        body.append("<div style='padding:32px 24px 24px 24px;background:#fff;border-radius:0 0 16px 16px;'>");
        body.append("<div style='font-size:1.1rem;color:#232b39;'>");
        body.append("<p>Dear <b>" + (customerName != null ? customerName : "Valued Customer") + "</b>,</p>");
        String formattedNow = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.now());
        if ("approved".equals(status)) {
            body.append("<p>🎉 <b>Great News!</b> Your quote has been <b>approved</b> and your order is now moving forward. We're excited to begin working on your pallet order and ensure it meets your exact requirements.</p>");
            body.append("<h3 style='color:#ffe082;margin-top:2rem;margin-bottom:0.5rem;'>Order Details</h3>");
            body.append("<table style='width:100%;border-collapse:collapse;margin-bottom:1.5rem;'>");
            body.append("<tr><td style='padding:6px 0;font-weight:600;'>Quote ID:</td><td>" + quoteId + "</td></tr>");
            body.append("<tr><td style='padding:6px 0;font-weight:600;'>Status:</td><td>Approved</td></tr>");
            body.append("<tr><td style='padding:6px 0;font-weight:600;'>Action Date:</td><td>" + formattedNow + "</td></tr>");
            if (quoteDetails != null) {
                body.append("<tr><td style='padding:6px 0;font-weight:600;'>Pallet Type:</td><td>" + (quoteDetails.get("palletType") != null ? quoteDetails.get("palletType") : "N/A") + "</td></tr>");
                body.append("<tr><td style='padding:6px 0;font-weight:600;'>Quantity:</td><td>" + (quoteDetails.get("quantity") != null ? quoteDetails.get("quantity") : "N/A") + "</td></tr>");
                if (quoteDetails.get("totalPrice") != null) {
                    body.append("<tr><td style='padding:6px 0;font-weight:600;'>Total Price:</td><td>₹" + quoteDetails.get("totalPrice") + "</td></tr>");
                }
            }
            body.append("</table>");
            body.append("<div style='margin:1.5rem 0 1rem 0;'><b>✅ What Happens Next:</b></div>");
            body.append("<ol style='margin:0 0 1.5rem 1.2rem;padding:0;color:#232b39;'><li>Our production team will review your specifications in detail.</li><li>We'll contact you within 24-48 hours to confirm your production timeline.</li><li>You'll receive regular updates on your order's progress until delivery.</li></ol>");
            body.append("<p>If you have any questions or wish to discuss further details, feel free to contact us at any time. We're here to help!</p>");
            body.append("<p>Thank you for choosing Saravana Timbers. We appreciate your business and look forward to serving you.</p>");
        } else if ("in production".equals(status)) {
            body.append("<p>Great News! Your Order Is Now in <b>Production</b>.</p>");
            body.append("<p>We're excited to let you know that your order (Order ID: <b>" + quoteId + "</b>) has officially moved into production. Our skilled team has started working on crafting your pallets with care and precision.</p>");
            body.append("<p>We'll keep you updated as your order progresses and will notify you as soon as it's ready for dispatch.</p>");
            body.append("<p>If you have any questions or need further assistance, feel free to reply to this email or reach out through your account dashboard.</p>");
        } else if ("rejected".equals(status)) {
            body.append("<h3 style='color:#f44336;margin-top:2rem;margin-bottom:0.5rem;'>Quote Status Update</h3>");
            body.append("<p><b>Status:</b> REJECTED</p>");
            body.append("<p><b>Action Date:</b> " + formattedNow + "</p>");
            body.append("<p>We regret to inform you that your quote has been rejected. This may be due to various factors such as:</p>");
            body.append("<ul style='color:#232b39;'><li>Specifications that don't meet our production capabilities</li><li>Pricing constraints</li><li>Material availability issues</li></ul>");
            if (quoteDetails != null) {
                body.append("<p><b>Quote Details:</b></p><ul style='color:#232b39;'>");
                body.append("<li>Pallet Type: " + (quoteDetails.get("palletType") != null ? quoteDetails.get("palletType") : "N/A") + "</li>");
                body.append("<li>Quantity: " + (quoteDetails.get("quantity") != null ? quoteDetails.get("quantity") : "N/A") + "</li>");
                if (quoteDetails.get("totalPrice") != null) {
                    body.append("<li>Requested Price: ₹" + quoteDetails.get("totalPrice") + "</li>");
                }
                body.append("</ul>");
            }
            body.append("<p>We encourage you to:<ol style='margin:0 0 1.5rem 1.2rem;padding:0;color:#232b39;'><li>Contact our sales team for alternative solutions</li><li>Discuss your requirements to find a suitable option</li><li>Submit a new quote with modified specifications</li></ol></p>");
        } else if ("cancelled".equals(status)) {
            body.append("<h3 style='color:#f44336;margin-top:2rem;margin-bottom:0.5rem;'>Quote Cancellation Notice</h3>");
            body.append("<p><b>Status:</b> CANCELLED</p>");
            body.append("<p><b>Cancellation Date:</b> " + formattedNow + "</p>");
            body.append("<p>Your quote has been cancelled as requested.</p>");
            if (quoteDetails != null) {
                body.append("<p><b>Cancelled Quote Details:</b></p><ul style='color:#232b39;'>");
                body.append("<li>Pallet Type: " + (quoteDetails.get("palletType") != null ? quoteDetails.get("palletType") : "N/A") + "</li>");
                body.append("<li>Quantity: " + (quoteDetails.get("quantity") != null ? quoteDetails.get("quantity") : "N/A") + "</li>");
                body.append("</ul>");
            }
            body.append("<p>If you need any assistance or wish to place a new order, please don't hesitate to contact us.</p>");
        } else if ("dispatched".equals(status)) {
            body.append("<h3 style='color:#4caf50;margin-top:2rem;margin-bottom:0.5rem;'>Your Order is On the Way! 🚚</h3>");
            body.append("<p>Good news — your order (Order ID: <b>" + quoteId + "</b>) has been dispatched and is on its way to you!</p>");
            body.append("<p>You can track the progress or find more details anytime in your account dashboard. If you have any questions or need assistance, we're just a message away.</p>");
            body.append("<p>Thank you once again for choosing Saravana Timbers. We truly appreciate your trust and look forward to serving you again!</p>");
        }
        // Footer
        body.append("<div style='margin-top:2.5rem;padding-top:1.5rem;border-top:1px solid #eee;color:#888;font-size:0.95rem;'>");
        body.append("Best regards,<br/><b>Saravana Timbers Team</b><br/>");
        body.append("<div style='margin-top:0.5rem;'>");
        body.append("📞 +91-9788885558 &nbsp; | &nbsp; 🌐 www.saravanatimbers.com &nbsp; | &nbsp; ✉ saravanatimbers.web@gmail.com");
        body.append("</div>");
        body.append("<div style='margin-top:0.5rem;color:#aaa;font-size:0.9rem;'>Thank you for choosing Saravana Timbers!</div>");
        body.append("</div>");
        body.append("</div></div>");
        return body.toString();
    }

    // Legacy method for backward compatibility
    public void sendQuoteStatusEmail(String to, String quoteId, String status) {
        sendQuoteStatusEmail(to, quoteId, status, null, null);
    }
} 