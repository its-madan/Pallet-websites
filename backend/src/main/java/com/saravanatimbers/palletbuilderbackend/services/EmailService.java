package com.saravanatimbers.palletbuilderbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendInvoiceToCustomer(String to, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Invoice from Saravana Timbers");
            helper.setFrom("saravanatimbers.web@gmail.com");
            
            // Try to embed logo with better error handling
            try {
                ClassPathResource logoResource = new ClassPathResource("static/images/company-logo.png");
                if (logoResource.exists()) {
                    helper.addInline("logo", logoResource);
                    System.out.println("✅ Logo embedded successfully in invoice email");
                } else {
                    System.out.println("⚠️ Logo file not found at: static/images/company-logo.png");
                }
            } catch (Exception logoException) {
                System.err.println("❌ Failed to embed logo in invoice email: " + logoException.getMessage());
                logoException.printStackTrace();
            }
            
            // HTML body with branding (no logo)
            String body = "<div style='font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f7;border-radius:16px;box-shadow:0 2px 12px rgba(44,44,44,0.07);overflow:hidden;'>" +
                "<div style='background:#ffe082;padding:20px 0 16px 0;text-align:center;border-bottom:1px solid #f3e5ab;border-radius:0 0 16px 16px;'>" +
                "<span style='font-size:2rem;font-weight:700;color:#232b39;letter-spacing:1px;display:block;'>SARAVANA TIMBERS</span>" +
                "<span style='color:#232b39;font-size:1rem;display:block;margin-top:4px;'>GSTIN: 33ASPPS0683QIZU &nbsp; | &nbsp; Phone: 9788885558</span>" +
                "<span style='color:#232b39;font-size:0.95rem;display:block;margin-top:2px;'>No 7,Thudiyalur road, Saravanampatti, Coimbatore - 641035</span>" +
                "</div>" +
                "<div style='height:1px;background:#eee;'></div>" +
                "<div style='padding:32px 24px 24px 24px;background:#fff;border-radius:0 0 16px 16px;'>" +
                "<div style='font-size:1.1rem;color:#232b39;'>" +
                "<p>Dear Customer,</p>" +
                "<p>Please find attached your invoice.</p>" +
                "<p>Thank you for your business!</p>" +
                "</div>" +
                "<div style='margin-top:2.5rem;padding-top:1.5rem;border-top:1px solid #eee;color:#888;font-size:0.95rem;'>" +
                "Best regards,<br/><b>Saravana Timbers Team</b><br/>" +
                "<div style='margin-top:0.5rem;'>" +
                "📞 +91-9788885558 &nbsp; | &nbsp; 🌐 www.saravanatimbers.com &nbsp; | &nbsp; ✉ saravanatimbers.web@gmail.com" +
                "</div>" +
                "<div style='margin-top:0.5rem;color:#aaa;font-size:0.9rem;'>Thank you for choosing Saravana Timbers!</div>" +
                "</div></div></div>";
            helper.setText(body, true);
            // Attach the PDF
            helper.addAttachment("invoice.pdf", new org.springframework.core.io.ByteArrayResource(pdfBytes));
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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