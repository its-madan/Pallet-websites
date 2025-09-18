Write-Host "Testing Email Service for Pallet Builder Backend" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

$email = Read-Host "Enter email address to test"

Write-Host ""
Write-Host "Sending test email to: $email" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/quotes/test-email?email=$email" -Method POST
    Write-Host "Response: $response" -ForegroundColor Green
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test completed. Check the console output above and your email inbox." -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to continue" 