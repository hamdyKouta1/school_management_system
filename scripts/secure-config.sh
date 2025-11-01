#!/bin/bash
# Script to secure configuration files

echo "Securing application configuration files..."

# Create secure config directory
mkdir -p /etc/app
mkdir -p ./config

# Set restrictive permissions on config directories
chmod 750 /etc/app 2>/dev/null || echo "Warning: Could not set permissions on /etc/app"
chmod 750 ./config

# Set restrictive permissions on secure config files
if [ -f "/etc/app/secure.properties" ]; then
    chmod 600 /etc/app/secure.properties
    echo "Secured /etc/app/secure.properties (owner read/write only)"
fi

if [ -f "./config/secure.properties" ]; then
    chmod 600 ./config/secure.properties
    echo "Secured ./config/secure.properties (owner read/write only)"
fi

# Set application.properties to read-only
if [ -f "./src/main/resources/application.properties" ]; then
    chmod 644 ./src/main/resources/application.properties
    echo "Set application.properties to read-only"
fi

echo "Configuration security setup complete!"
echo ""
echo "Security recommendations:"
echo "1. Store sensitive data in external config files"
echo "2. Use environment variables for production"
echo "3. Never commit secure.properties to version control"
echo "4. Regularly rotate passwords and secrets"
echo "5. Use proper file permissions (600 for sensitive files)"