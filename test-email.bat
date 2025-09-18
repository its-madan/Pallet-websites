@echo off
echo Testing Email Service for Pallet Builder Backend
echo ================================================
echo.

set /p email="Enter email address to test: "

echo.
echo Sending test email to: %email%
echo.

curl -X POST "http://localhost:8080/api/quotes/test-email?email=%email%"

echo.
echo.
echo Test completed. Check the console output above and your email inbox.
echo.
pause 