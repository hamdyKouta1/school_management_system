# Simple Bulk Upload Test Script - Manual Multipart Construction
# Server is running on port 8082

# Step 1: Authenticate and get JWT token
Write-Host "Step 1: Authenticating with developer credentials..." -ForegroundColor Yellow
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8082/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"developer","password":"123456789"}'

if ($loginResponse.StatusCode -eq 200) {
    Write-Host "Authentication successful!" -ForegroundColor Green
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $token = $loginData.token
    Write-Host "JWT Token obtained: $($token.Substring(0,50))..." -ForegroundColor Green
    
    # Step 2: Upload Excel file using manual multipart construction
    Write-Host "\nStep 2: Uploading student_batch_template.xlsx..." -ForegroundColor Yellow
    
    # Check if file exists
    $filePath = "student_batch_template.xlsx"
    if (Test-Path $filePath) {
        Write-Host "File found: $filePath" -ForegroundColor Green
        
        try {
            # Read file as bytes
            $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
            
            # Create boundary
            $boundary = [System.Guid]::NewGuid().ToString()
            $LF = "`r`n"
            
            # Construct multipart body manually
            $bodyLines = @(
                "--$boundary",
                "Content-Disposition: form-data; name=`"file`"; filename=`"student_batch_template.xlsx`"",
                "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "",
                [System.Text.Encoding]::Latin1.GetString($fileBytes),
                "--$boundary--",
                ""
            )
            
            $body = $bodyLines -join $LF
            
            # Set headers
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "multipart/form-data; boundary=$boundary"
            }
            
            # Send request
            Write-Host "Sending multipart request..." -ForegroundColor Yellow
            $response = Invoke-WebRequest -Uri "http://localhost:8082/api/protected/bulk-action/student-batch" -Method POST -Headers $headers -Body $body
            
            Write-Host "\nUpload Response:" -ForegroundColor Yellow
            Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Cyan
            Write-Host "Response Content:" -ForegroundColor Cyan
            Write-Host $response.Content -ForegroundColor White
            
            if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 201) {
                Write-Host "\nBulk upload completed successfully!" -ForegroundColor Green
            } else {
                Write-Host "\nUpload failed with status code: $($response.StatusCode)" -ForegroundColor Red
            }
            
        } catch {
            Write-Host "\nUpload failed with error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.Exception.Response) {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $responseBody = $reader.ReadToEnd()
                Write-Host "Error response: $responseBody" -ForegroundColor Red
            }
        }
        
    } else {
        Write-Host "Error: File not found - $filePath" -ForegroundColor Red
    }
    
} else {
    Write-Host "Authentication failed with status code: $($loginResponse.StatusCode)" -ForegroundColor Red
    Write-Host "Response: $($loginResponse.Content)" -ForegroundColor Red
}

Write-Host "\nTest completed." -ForegroundColor Yellow