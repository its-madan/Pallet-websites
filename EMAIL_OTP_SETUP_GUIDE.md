# Email OTP Setup Guide for Saravana Timbers

## Overview
This guide will help you set up the email OTP (One-Time Password) functionality for user email verification during signup.

## Prerequisites
- Gmail account with 2-factor authentication enabled
- Java 21 and Maven installed
- MongoDB running locally

## Step 1: Configure Gmail App Password

### 1.1 Enable 2-Factor Authentication
1. Go to your Google Account settings: https://myaccount.google.com/
2. Navigate to "Security"
3. Enable "2-Step Verification" if not already enabled

### 1.2 Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select "Mail" as the app
3. Select "Other (Custom name)" as device
4. Enter "Saravana Timbers Backend" as the name
5. Click "Generate"
6. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

## Step 2: Update Email Configuration

### 2.1 Edit application.properties
Open `backend/src/main/resources/application.properties` and update:

```properties
# Replace with your actual Gmail address
spring.mail.username=your.email@gmail.com

# Replace with the 16-character app password (remove spaces)
spring.mail.password=abcdefghijklmnop
```

### 2.2 Example Configuration
```properties
spring.mail.username=saravana.timbers@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

## Step 3: Test the Email OTP Service

### 3.1 Start the Backend
```bash
cd backend
mvn spring-boot:run
```

### 3.2 Test the Service
1. Open your browser and go to: `http://localhost:8080/api/auth/test-email-otp`
2. You should see a success response
3. Check the console logs for the generated OTP

### 3.3 Test with Frontend
1. Start the frontend: `cd pallet-builder && npm run dev`
2. Go to the signup page
3. Enter a valid email address
4. Click "Verify" button
5. Check your email for the OTP
6. Enter the OTP and click "Submit"

## Step 4: Troubleshooting

### 4.1 Common Issues

#### Issue: "Failed to send OTP"
**Solution:**
- Verify Gmail credentials are correct
- Ensure 2-factor authentication is enabled
- Check that app password is copied correctly (no extra spaces)
- Verify internet connection

#### Issue: "Invalid OTP"
**Solution:**
- OTP expires after 10 minutes
- Ensure you're entering the exact 6-digit code
- Check for extra spaces or characters

#### Issue: "Email service not working"
**Solution:**
- Check if Gmail SMTP is blocked by firewall
- Verify port 587 is not blocked
- Try using port 465 with SSL instead

### 4.2 Alternative Email Providers

If Gmail doesn't work, you can use other providers:

#### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your.email@outlook.com
spring.mail.password=your-app-password
```

#### Yahoo
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your.email@yahoo.com
spring.mail.password=your-app-password
```

## Step 5: Security Considerations

### 5.1 Rate Limiting
The current implementation includes basic rate limiting:
- OTP expires after 10 minutes
- OTP is removed after successful verification
- Failed attempts don't block the service

### 5.2 Production Deployment
For production, consider:
- Using environment variables for email credentials
- Implementing proper rate limiting
- Adding CAPTCHA for OTP requests
- Using a dedicated email service (SendGrid, Mailgun, etc.)

## Step 6: Email Template Customization

### 6.1 Current Email Template
The email template is in `OtpEmailService.java`:

```java
message.setText(
    "Hello,\n\n" +
    "Your email verification code is: " + otp + "\n\n" +
    "This code will expire in " + OTP_EXPIRY_MINUTES + " minutes.\n\n" +
    "If you didn't request this code, please ignore this email.\n\n" +
    "Best regards,\n" +
    "Saravana Timbers Team"
);
```

### 6.2 Customizing the Template
You can modify the email content by editing the `sendOtp` method in `OtpEmailService.java`.

## Step 7: Monitoring and Logs

### 7.1 Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.org.springframework.mail=DEBUG
logging.level.com.saravanatimbers.palletbuilderbackend=DEBUG
```

### 7.2 Check Logs
Monitor the console output for:
- Email sending success/failure
- OTP generation
- Verification attempts

## Success Indicators

✅ **Email OTP is working when:**
- You receive an email with a 6-digit OTP
- The OTP can be verified successfully
- The frontend shows "Email verified successfully"
- Users can complete the signup process

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Verify all configuration steps are completed
3. Check the application logs for error messages
4. Test with the `/api/auth/test-email-otp` endpoint

---

**Note:** This setup is for development/testing. For production deployment, use proper email service providers and security measures. 