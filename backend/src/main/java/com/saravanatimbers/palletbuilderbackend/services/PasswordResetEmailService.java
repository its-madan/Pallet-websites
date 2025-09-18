package com.saravanatimbers.palletbuilderbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
public class PasswordResetEmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Saravana Timbers");
            helper.setFrom("saravanatimbers.web@gmail.com");
            
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
                "<p>We received a request to reset your password.</p>" +
                "<p>Click the link below to set a new password (valid for 30 minutes):</p>" +
                "<p><a href='" + resetLink + "' style='display:inline-block;padding:10px 24px;background:#ffe082;color:#232b39;text-decoration:none;border-radius:5px;font-weight:600;'>Reset Password</a></p>" +
                "<p>If you didn't request this, you can safely ignore this email.</p>" +
                "<p>Best regards,<br/>Saravana Timbers Team</p>" +
                "</div>" +
                "<div style='margin-top:2.5rem;padding-top:1.5rem;border-top:1px solid #eee;color:#888;font-size:0.95rem;'>" +
                "📞 +91-9788885558 &nbsp; | &nbsp; 🌐 www.saravanatimbers.com &nbsp; | &nbsp; ✉ saravanatimbers.web@gmail.com" +
                "<div style='margin-top:0.5rem;color:#aaa;font-size:0.9rem;'>Thank you for choosing Saravana Timbers!</div>" +
                "</div></div></div>";
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 