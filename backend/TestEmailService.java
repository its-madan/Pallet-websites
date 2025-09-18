import java.util.HashMap;
import java.util.Map;

// Simple test to verify email service functionality
public class TestEmailService {
    public static void main(String[] args) {
        // This is just a reference for testing the email service
        // The actual implementation is in QuoteEmailService.java
        
        System.out.println("Email Service Test Reference");
        System.out.println("============================");
        System.out.println();
        System.out.println("To test the email functionality:");
        System.out.println("1. Start the Spring Boot application");
        System.out.println("2. Submit a quote through the frontend");
        System.out.println("3. Login as admin and approve/reject the quote");
        System.out.println("4. Check the console logs for email sending status");
        System.out.println("5. Check the recipient's email inbox");
        System.out.println();
        System.out.println("Expected email content for approved quotes:");
        System.out.println("- Subject: Quote Status Update - Saravana Timbers");
        System.out.println("- Contains: Quote ID, approval message, order details");
        System.out.println("- Contains: Next steps and contact information");
        System.out.println();
        System.out.println("Expected email content for rejected quotes:");
        System.out.println("- Subject: Quote Status Update - Saravana Timbers");
        System.out.println("- Contains: Quote ID, rejection message, quote details");
        System.out.println("- Contains: Alternative suggestions and contact information");
    }
} 