@echo off
echo ========================================
echo    Email OTP Configuration Test
echo ========================================
echo.

echo Step 1: Checking if backend is running...
curl -s http://localhost:8080/api/auth/test-email-otp > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend is running
) else (
    echo ❌ Backend is not running. Please start it first:
    echo    cd backend
    echo    mvn spring-boot:run
    pause
    exit /b 1
)

echo.
echo Step 2: Testing email OTP service...
curl -s http://localhost:8080/api/auth/test-email-otp
echo.

echo.
echo Step 3: Manual Testing Instructions
echo ========================================
echo 1. Open your browser and go to: http://localhost:3000
echo 2. Click "Login" then "Click here to Sign Up"
echo 3. Enter a valid email address
echo 4. Click "Verify" button
echo 5. Check your email for the OTP
echo 6. Enter the OTP and click "Submit"
echo.

echo If you see errors, check:
echo - Gmail credentials in application.properties
echo - 2-factor authentication is enabled
echo - App password is correct
echo - Internet connection is working
echo.

pause 