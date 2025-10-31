@echo off
echo Testing Bulk Student Upload Endpoint
echo ======================================
echo.
echo Step 1: Login to get authentication token
curl -s -X POST -H "Content-Type: application/json" ^-d "{\"username\":\"developer\",\"password\":\"123456789\"}" ^http://localhost:8081/api/auth/login > login_response.json

echo Step 2: Extract token (manual step required)
echo Please extract the token from login_response.json and set it as TOKEN variable
echo.
echo Step 3: Upload Excel file
echo curl -X POST ^
echo   -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
echo   -H "Accept: application/json" ^
echo   -F "file=@student_batch_template.xlsx" ^
echo   http://localhost:8081/api/protected/bulk-action/student-batch
echo.
echo Note: Replace YOUR_TOKEN_HERE with the actual token from step 1
echo Make sure the server is running on localhost:8081
pause