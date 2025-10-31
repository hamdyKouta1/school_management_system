# PowerShell script to test bulk student upload endpoint
# Note: This assumes the server is running on localhost:8081

$baseUrl = "http://localhost:8082"
$username = "developer"
$password = "123456789"
$excelFile = "student_batch_template.xlsx"

Write-Host "Testing Bulk Student Upload Endpoint" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# Step 1: Login to get token
Write-Host "Step 1: Authenticating with credentials: $username" -ForegroundColor Yellow

$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Authentication successful. Token obtained." -ForegroundColor Green
} catch {
    Write-Host "✗ Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the server is running on $baseUrl" -ForegroundColor Yellow
    exit 1
}

# Step 2: Test bulk upload
Write-Host "\nStep 2: Testing bulk upload with file: $excelFile" -ForegroundColor Yellow

if (-not (Test-Path $excelFile)) {
    Write-Host "✗ Excel file not found: $excelFile" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}

# Create multipart form data
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"

$bodyLines = (
    "--$boundary",
    "Content-Disposition: form-data; name=\"file\"; filename=\"$excelFile\"",
    "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "",
    [System.Text.Encoding]::Latin1.GetString([System.IO.File]::ReadAllBytes($excelFile)),
    "--$boundary--",
    ""
) -join $LF

$headers["Content-Type"] = "multipart/form-data; boundary=$boundary"

try {
    Write-Host "Uploading Excel file..." -ForegroundColor Yellow
    $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/api/protected/bulk-action/student-batch" -Method POST -Body $bodyLines -Headers $headers
    
    Write-Host "✓ Upload successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    $uploadResponse | ConvertTo-Json -Depth 10 | Write-Host
    
} catch {
    Write-Host "✗ Upload failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody" -ForegroundColor Red
    }
}

Write-Host "\nTest completed." -ForegroundColor Green