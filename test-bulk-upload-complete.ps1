# Complete Bulk Upload Test Script - .NET HttpClient Version
# Server is running on port 8081

Add-Type -AssemblyName System.Net.Http

# Step 1: Authenticate and get JWT token
Write-Host "Step 1: Authenticating with developer credentials..."
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"developer","password":"123456789"}'

if ($loginResponse.StatusCode -eq 200) {
    Write-Host "Authentication successful!" -ForegroundColor Green
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $token = $loginData.token
    Write-Host "JWT Token obtained: $($token.Substring(0,50))..." -ForegroundColor Green
    
    # Step 2: Upload Excel file using .NET HttpClient
    Write-Host "\nStep 2: Uploading student_batch_template.xlsx..."
    
    # Check if file exists
    $filePath = "student_batch_template.xlsx"
    if (Test-Path $filePath) {
        Write-Host "File found: $filePath" -ForegroundColor Green
        
        try {
            # Create HttpClient
            $httpClient = New-Object System.Net.Http.HttpClient
            $httpClient.DefaultRequestHeaders.Add("Authorization", "Bearer $token")
            
            # Create multipart form data content (let .NET generate a proper boundary)
            $multipartContent = New-Object System.Net.Http.MultipartFormDataContent
            
            # Read file and add to form
            $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
            # Use constructor invocation with ::new to avoid ArgumentList enumeration issues
            $fileContent = [System.Net.Http.ByteArrayContent]::new($fileBytes)
            # Optionally set the file content type
            $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            $multipartContent.Add($fileContent, "file", "student_batch_template.xlsx")

            # Debug: show the Content-Type header for the multipart body
            Write-Host "Multipart Content-Type: $($multipartContent.Headers.ContentType)" -ForegroundColor Yellow
            
            # Send request (ensure trailing slash to match servlet mapping /student-batch/*)
            $uploadUrl = "http://localhost:8081/api/protected/bulk-action/student-batch/"
            Write-Host "Uploading to: $uploadUrl" -ForegroundColor Yellow
            $response = $httpClient.PostAsync($uploadUrl, $multipartContent).Result
            $responseContent = $response.Content.ReadAsStringAsync().Result
            
            Write-Host "\nUpload Response:" -ForegroundColor Yellow
            Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Cyan
            Write-Host "Response Content:" -ForegroundColor Cyan
            Write-Host $responseContent -ForegroundColor White
            
            if ($response.IsSuccessStatusCode) {
                Write-Host "\nBulk upload completed successfully!" -ForegroundColor Green
            } else {
                Write-Host "\nUpload failed with status code: $($response.StatusCode)" -ForegroundColor Red
            }
            
            # Cleanup
            $httpClient.Dispose()
            
        } catch {
            Write-Host "\nUpload failed with error: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "Stack trace: $($_.Exception.StackTrace)" -ForegroundColor Red
        }
        
    } else {
        Write-Host "Error: File not found - $filePath" -ForegroundColor Red
    }
    
} else {
    Write-Host "Authentication failed with status code: $($loginResponse.StatusCode)" -ForegroundColor Red
    Write-Host "Response: $($loginResponse.Content)" -ForegroundColor Red
}

Write-Host "\nTest completed." -ForegroundColor Yellow