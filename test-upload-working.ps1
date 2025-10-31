# Final Working Bulk Upload Test Script using WebClient
# Server is running on port 8082

Write-Host "Testing Bulk Student Upload Endpoint" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# Step 1: Login to get token
Write-Host "Step 1: Authenticating with developer credentials..." -ForegroundColor Yellow

$loginBody = @{
    username = "developer"
    password = "123456789"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Authentication successful. Token obtained." -ForegroundColor Green
} catch {
    Write-Host "Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the server is running on http://localhost:8082" -ForegroundColor Yellow
    exit 1
}

# Step 2: Test bulk upload
Write-Host "Step 2: Testing bulk upload with file: student_batch_template.xlsx" -ForegroundColor Yellow

$excelFile = "student_batch_template.xlsx"
if (-not (Test-Path $excelFile)) {
    Write-Host "Excel file not found: $excelFile" -ForegroundColor Red
    exit 1
}

# Use WebClient for multipart upload
Add-Type -AssemblyName System.Net

try {
    Write-Host "Uploading Excel file using WebClient..." -ForegroundColor Yellow
    
    # Create WebClient
    $webClient = New-Object System.Net.WebClient
    $webClient.Headers.Add("Authorization", "Bearer $token")
    
    # Create boundary and multipart data
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    # Read file as bytes
    $fileBytes = [System.IO.File]::ReadAllBytes($excelFile)
    
    # Build multipart body manually
    $bodyLines = @()
    $bodyLines += "--$boundary"
    $bodyLines += 'Content-Disposition: form-data; name="file"; filename="student_batch_template.xlsx"'
    $bodyLines += "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    $bodyLines += ""
    
    # Convert body lines to bytes
    $headerBytes = [System.Text.Encoding]::UTF8.GetBytes(($bodyLines -join $LF) + $LF)
    $footerBytes = [System.Text.Encoding]::UTF8.GetBytes($LF + "--$boundary--" + $LF)
    
    # Combine all bytes
    $totalBytes = New-Object byte[] ($headerBytes.Length + $fileBytes.Length + $footerBytes.Length)
    [Array]::Copy($headerBytes, 0, $totalBytes, 0, $headerBytes.Length)
    [Array]::Copy($fileBytes, 0, $totalBytes, $headerBytes.Length, $fileBytes.Length)
    [Array]::Copy($footerBytes, 0, $totalBytes, $headerBytes.Length + $fileBytes.Length, $footerBytes.Length)
    
    # Set content type
    $webClient.Headers.Add("Content-Type", "multipart/form-data; boundary=$boundary")
    
    # Upload data
    $responseBytes = $webClient.UploadData("http://localhost:8082/api/protected/bulk-action/student-batch", "POST", $totalBytes)
    $responseText = [System.Text.Encoding]::UTF8.GetString($responseBytes)
    
    Write-Host "Upload successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host $responseText -ForegroundColor White
    
    # Cleanup
    $webClient.Dispose()
    
} catch [System.Net.WebException] {
    Write-Host "Upload failed with WebException: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody" -ForegroundColor Red
        $reader.Close()
    }
    if ($webClient) {
        $webClient.Dispose()
    }
} catch {
    Write-Host "Upload failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($webClient) {
        $webClient.Dispose()
    }
}

Write-Host "Test completed." -ForegroundColor Green