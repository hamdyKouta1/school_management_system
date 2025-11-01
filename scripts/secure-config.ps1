# PowerShell script to secure configuration files on Windows

Write-Host "Securing application configuration files..." -ForegroundColor Green

# Create secure config directory
$configDir = ".\config"
if (!(Test-Path $configDir)) {
    New-Item -ItemType Directory -Path $configDir -Force
    Write-Host "Created config directory: $configDir" -ForegroundColor Yellow
}

# Create example secure.properties if it doesn't exist
$secureConfigPath = "$configDir\secure.properties"
if (!(Test-Path $secureConfigPath)) {
    $secureContent = @"
# Secure configuration file - Keep outside version control
# This file should be placed in a secure location with restricted access

# Email credentials
email.password=your-secure-email-password

# Database credentials  
db.password=your-secure-db-password

# JWT secret (256-bit minimum)
jwt.secret=your-256-bit-jwt-secret-key-here

# Admin credentials
admin.secret_code=your-secure-admin-code
"@
    
    Set-Content -Path $secureConfigPath -Value $secureContent
    Write-Host "Created example secure config: $secureConfigPath" -ForegroundColor Yellow
}

# Set file permissions (Windows ACL)
try {
    # Get current user
    $currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    
    # Set restrictive permissions on secure config file
    $acl = Get-Acl $secureConfigPath
    $acl.SetAccessRuleProtection($true, $false)  # Disable inheritance
    
    # Add full control for current user only
    $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($currentUser, "FullControl", "Allow")
    $acl.SetAccessRule($accessRule)
    
    Set-Acl -Path $secureConfigPath -AclObject $acl
    Write-Host "Secured $secureConfigPath (current user access only)" -ForegroundColor Green
    
} catch {
    Write-Host "Warning: Could not set file permissions. Please manually secure the file." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Configuration security setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Security recommendations:" -ForegroundColor Cyan
Write-Host "1. Store sensitive data in external config files" -ForegroundColor White
Write-Host "2. Use environment variables for production" -ForegroundColor White
Write-Host "3. Never commit secure.properties to version control" -ForegroundColor White
Write-Host "4. Regularly rotate passwords and secrets" -ForegroundColor White
Write-Host "5. Use proper file permissions for sensitive files" -ForegroundColor White
Write-Host ""
Write-Host "Usage examples:" -ForegroundColor Cyan
Write-Host "# Set environment variable:" -ForegroundColor White
Write-Host "`$env:EMAIL_PASSWORD='your-secure-password'" -ForegroundColor Gray
Write-Host ""
Write-Host "# Specify secure config path:" -ForegroundColor White
Write-Host "java -Dconfig.secure.path=.\config\secure.properties -jar school-management.jar" -ForegroundColor Gray